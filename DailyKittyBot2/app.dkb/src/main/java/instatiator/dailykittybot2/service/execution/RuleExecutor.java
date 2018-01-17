package instatiator.dailykittybot2.service.execution;

import android.content.Context;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.data.RunReportCollation;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.RunReport;
import instatiator.dailykittybot2.service.IBotService;
import instatiator.dailykittybot2.service.RedditSession;
import instatiator.dailykittybot2.service.tasks.RunResult;
import instatiator.dailykittybot2.validation.RuleValidator;
import instatiator.dailykittybot2.validation.ValidationResult;

public class RuleExecutor {
    public static final long ONE_HOUR = 1000 * 60 * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;
    public static final long ONE_WEEK = ONE_DAY * 7;
    public static final long ONE_MONTH = ONE_WEEK * 4;

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

    public List<RuleResult> execute_rules_for_subreddit(String subreddit, List<RuleTriplet> rules, ExecutionMode mode) {
        listener.testing_subreddit(subreddit);

        List<RuleResult> results = new LinkedList<>();
        RedditClient reddit = session.getClient();

        // TODO: bad - the rules do NOT track their last runs, and ALSO we should use the RunReports!
        List<TimePeriod> periods = determine_best_timeperiods(rules, mode);
        TimePeriod longest = longest_from(periods);
        // TODO: do these give sensible answers?

        Map<RuleTriplet, Date> rules_to_last_report = new HashMap<>();
        for (RuleTriplet triplet : rules) {
            RunReport report = service.get_workspace().get_last_report_for(session.get_username(), subreddit, triplet.rule.uuid);
            if (report != null) {
                rules_to_last_report.put(triplet, report.lastConsideredItemDate);
            }
        }

        SubredditReference subreddit_ref = reddit.subreddit(subreddit);

        DefaultPaginator<Submission> paginator =
                subreddit_ref
                        .posts()
                        .timePeriod(longest)
                        .sorting(SubredditSort.NEW)
                        .build();

        for (Listing<Submission> page : paginator) {
            for (Submission submission : page.getChildren()) {
                listener.testing_submission(submission);

                for (RuleTriplet triplet : rules) {
                    listener.applying_rule(triplet);

                    RuleResult result = new RuleResult();
                    result.rule = triplet;
                    result.username = reddit.me().getUsername();
                    result.subreddit = subreddit;
                    result.submission = submission;

                    RuleValidator validator = new RuleValidator(context);
                    ValidationResult validation = validator.validate(triplet);
                    result.validates = validation.validates;
                    result.errors = validation.errors;
                    result.warnings = validation.warnings;

                    if (validation.validates) {
                        Date latest_from_last_run = rules_to_last_report.get(triplet);
                        if (rule_matches(triplet, submission, latest_from_last_run)) {
                            result.matched = true;
                            result.recommendations = generate_recommendations(triplet, submission);
                            listener.generated_recommendations(result.recommendations.size());
                        } else {
                            result.matched = false;
                        }
                        result.ran = true;
                    } // validated

                    results.add(result);
                } // rule
            } // submission
        } // listing

        return results;
    }

    public Collection<RunReport> collate_reports_for_subreddit(List<RuleResult> rule_results, Date started, Date end) {
        Map<UUID, RunReport> rule_to_run_reports = new HashMap<>();

        for (RuleResult result : rule_results) {
            if (!rule_to_run_reports.containsKey(result.rule.rule.uuid)) {
                RunReport rr = new RunReport();
                rr.uuid = UUID.randomUUID();
                rr.username = result.username;
                rr.subreddit = result.subreddit;
                rr.ruleUuid = result.rule.rule.uuid;
                rr.started = started;
                rr.finished = end;
                rule_to_run_reports.put(result.rule.rule.uuid, rr);
            }

            RunReport report = rule_to_run_reports.get(result.rule.rule.uuid);
            if (report.lastConsideredItemDate == null || result.submission.getCreated().after(report.lastConsideredItemDate)) {
                report.lastConsideredItemDate = result.submission.getCreated();
            }

            if (result.recommendations != null) {
                for (Recommendation recommendation : result.recommendations) {
                    recommendation.runReportUuid = report.uuid;
                }
            }
        }

        return rule_to_run_reports.values();
    }

    private boolean rule_matches(RuleTriplet rule, Submission submission, Date latest_previously) {
        if (latest_previously == null || submission.getCreated().after(latest_previously)) {
            boolean ok = true;
            for (Condition condition : rule.conditions) {
                if (!condition_matches(condition, submission)) {
                    ok = false;
                    break;
                }
            }
            return ok;
        } else {
            return false; // is from before the cut off!
        }
    }

    private boolean condition_matches(Condition condition, Submission submission) {
        return matcher.condition_matches(condition, submission);
    }

    private List<Recommendation> generate_recommendations(RuleTriplet rule, Submission submission) {
        return generator.generate_recommendations_for(rule, submission);
    }

    private static final TimePeriod TIMEPERIOD_UnhelpfulDefault = TimePeriod.DAY;

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
