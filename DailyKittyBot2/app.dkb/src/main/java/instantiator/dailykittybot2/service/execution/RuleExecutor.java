package instantiator.dailykittybot2.service.execution;

import android.content.Context;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.Paginator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.validation.RuleValidator;
import instantiator.dailykittybot2.validation.ValidationResult;

public class RuleExecutor {
    public static final long ONE_HOUR = 1000 * 60 * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;
    public static final long ONE_WEEK = ONE_DAY * 7;
    public static final long ONE_MONTH = ONE_DAY * 31;
    public static final long ONE_YEAR = ONE_DAY * 365;

    private static final TimePeriod TIMEPERIOD_UnhelpfulDefault = TimePeriod.DAY;

    private Context context;
    private IBotService service;
    private RedditSession session;
    private Listener listener;
    private ConditionMatcher matcher;
    private RecommendationGenerator generator;

    public enum ExecutionMode {
        RespectRuleLastRun,
        ActOnLastHourSubmissions,
        ActOnLastDaySubmissions
    }

    public RuleExecutor(Context context, IBotService service, RedditSession session, Listener listener) {
        this.session = session;
        this.listener = listener;
        this.context = context;
        this.service = service;

        this.matcher = new ConditionMatcher(session.getClient());
        this.generator = new RecommendationGenerator();
    }

    public SubredditExecutionResult execute_rules_for_subreddit(String subreddit, List<RuleTriplet> triplets, ExecutionMode mode) {
        listener.testing_subreddit(subreddit);
        RedditClient reddit = session.getClient();
        String username = reddit.me().getUsername();
        Date start = new Date();

        // find the last considered item for each rule in this subreddit
        Map<UUID, Date> last_considerations = find_last_considered_items(triplets, subreddit);

        // prepare new run reports
        SubredditExecutionResult out_result = new SubredditExecutionResult();
        Map<UUID, RunReport> out_RunReports = prepare_blank_run_reports(triplets, username, subreddit);
        List<RuleResult> out_RuleResults = new LinkedList<>();

        // prepare time limits
        List<TimePeriod> periods = determine_best_timeperiods(triplets, mode);
        TimePeriod longest = longest_from(periods);

        // fetch submissions in range
        List<Submission> submissions = reddit
                .subreddit(subreddit)
                .posts()
                .sorting(SubredditSort.NEW)
                .timePeriod(longest)
                .limit(Paginator.RECOMMENDED_MAX_LIMIT)
                .build()
                .accumulateMerged(-1);

        for (Submission submission : submissions) {
            listener.testing_submission(submission);

            for (RuleTriplet triplet : triplets) {
                listener.applying_rule(triplet);

                RuleResult result = execute_rule_on_submission(triplet, submission, mode, longest, last_considerations);
                out_RuleResults.add(result);
            } // rule
        } // submission

        Date finished = new Date();

        // link rule results to reports
        for (RuleResult result : out_RuleResults) {
            RunReport report = out_RunReports.get(result.triplet.rule.uuid);
            report.started = start;
            report.finished = finished;

            if (report.lastConsideredItemDate == null ||
                result.submission.getCreated().after(report.lastConsideredItemDate)) {
                report.lastConsideredItemDate = result.submission.getCreated();
            }

            if (result.recommendations != null) {
                for (Recommendation recommendation : result.recommendations) {
                    recommendation.runReportUuid_unsafe = report.uuid;
                }
            }
        }

        out_result.subreddit_rule_RunReports = out_RunReports.values();
        out_result.subreddit_rule_RuleResults = out_RuleResults;
        return out_result;
    }

    private RuleResult execute_rule_on_submission(RuleTriplet triplet, Submission submission, ExecutionMode mode, TimePeriod limit, Map<UUID, Date> last_considerations) {
        RedditClient reddit = session.getClient();

        RuleResult result = new RuleResult();
        result.triplet = triplet;
        result.username = reddit.me().getUsername();
        result.subreddit = submission.getSubreddit();
        result.submission = submission;

        RuleValidator validator = new RuleValidator(context);
        ValidationResult validation = validator.validate(triplet);
        result.validates = validation.validates;
        result.errors = validation.errors;
        result.warnings = validation.warnings;

        if (validation.validates) {
            Date previous_latest = mode == ExecutionMode.RespectRuleLastRun ? last_considerations.get(triplet) : null;
            if (post_is_in_date(submission, previous_latest, limit) && rule_matches(triplet, submission)) {
                result.matched = true;
                result.recommendations = generate_recommendations(triplet, submission);
                listener.generated_recommendations(result.recommendations.size());
            } else {
                result.matched = false; // not a good match
            }

            result.ran = true;
        }

        return result;
    }

    private Map<UUID, RunReport> prepare_blank_run_reports(List<RuleTriplet> triplets, String username, String subreddit) {
        Map<UUID, RunReport> map = new HashMap<>();
        for (RuleTriplet triplet : triplets) {
            RunReport rr = new RunReport();
            rr.uuid = UUID.randomUUID();
            rr.username = username;
            rr.subreddit = subreddit;
            rr.ruleUuid = triplet.rule.uuid;
            map.put(triplet.rule.uuid, rr);
        }
        return map;
    }

    private Map<UUID, Date> find_last_considered_items(List<RuleTriplet> triplets, String subreddit) {
        Map<UUID, Date> map = new HashMap<>();
        for (RuleTriplet triplet : triplets) {
            RunReport report = service.get_workspace().get_last_report_for(session.get_username(), subreddit, triplet.rule.uuid);
            if (report != null) {
                map.put(triplet.rule.uuid, report.lastConsideredItemDate);
            }
        }
        return map;
    }

    private boolean post_is_in_date(Submission submission, Date latest_previously, TimePeriod limit) {
        if (latest_previously == null) {
            latest_previously = getDateFromTimePeriod(limit);
        }
        return submission.getCreated().after(latest_previously);
    }

    private Date getDateFromTimePeriod(TimePeriod period) {
        long time = new Date().getTime();
        switch (period) {
            case HOUR:
                time -= ONE_HOUR;
                break;
            case DAY:
                time -= ONE_DAY;
                break;
            case WEEK:
                time -= ONE_WEEK;
                break;
            case MONTH:
                time -= ONE_MONTH;
                break;
            case YEAR:
                time -= ONE_YEAR;
                break;
            case ALL:
                time = Long.MIN_VALUE;
                break;
        }
        return new Date(time);
    }

    private boolean rule_matches(RuleTriplet rule, Submission submission) {
        boolean ok = true;
        for (Condition condition : rule.conditions) {
            if (!condition_matches(condition, submission)) {
                ok = false;
                break;
            }
        }
        return ok;
    }

    private boolean condition_matches(Condition condition, Submission submission) {
        return matcher.condition_matches(condition, submission);
    }

    private List<Recommendation> generate_recommendations(RuleTriplet rule, Submission submission) {
        return generator.generate_recommendations_for(rule, submission);
    }

    public static List<TimePeriod> determine_best_timeperiods(List<RuleTriplet> rules, ExecutionMode mode) {
        if (mode == ExecutionMode.ActOnLastHourSubmissions) {
            return Arrays.asList(TimePeriod.HOUR);
        }
        if (mode == ExecutionMode.ActOnLastDaySubmissions) {
            return Arrays.asList(TimePeriod.DAY);
        }

        List<TimePeriod> list = new LinkedList<>();
        for (RuleTriplet rule : rules) {
            if (rule.rule.last_run == null) {
                list.add(TIMEPERIOD_UnhelpfulDefault); // if the rule hasn't run before...
            } else {
                Date now = new Date();
                long diff = now.getTime() - rule.rule.last_run.getTime();
                if (diff < ONE_HOUR) {
                    list.add(TimePeriod.HOUR);
                } else if (diff < ONE_DAY) {
                    list.add(TimePeriod.DAY);
                } else if (diff < ONE_WEEK) {
                    list.add(TimePeriod.WEEK);
                } else if (diff < ONE_MONTH) {
                    list.add(TimePeriod.MONTH);
                } else {
                    list.add(TimePeriod.ALL);
                }
            }
        }
        return list;
    }

    public static TimePeriod longest_from(List<TimePeriod> periods) {
        TimePeriod longest = null;
        for (TimePeriod period : periods) {
            if (longest == null) {
                longest = period;
            } else {
                if (period.compareTo(longest) > 0) {
                    longest = period;
                }
            }
        }
        return longest;
    }

    public static Map<String, List<RuleTriplet>> reorg_rules_per_subreddit(List<RuleTriplet> rules) {
        HashMap<String, List<RuleTriplet>> map = new HashMap<>();
        for (RuleTriplet rule : rules) {
            for (String rule_subreddit : rule.rule.subreddits) {
                if (!map.containsKey(rule_subreddit)) {
                    map.put(rule_subreddit, new LinkedList<RuleTriplet>());
                }
                map.get(rule_subreddit).add(rule);
            }
        }
        return map;
    }

    public interface Listener {
        void testing_subreddit(String subreddit);
        void testing_submission(Submission submission);
        void applying_rule(RuleTriplet rule);
        void generated_recommendations(int recommendations);
    }

}
