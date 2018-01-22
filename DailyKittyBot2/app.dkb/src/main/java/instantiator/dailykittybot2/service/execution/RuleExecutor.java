package instantiator.dailykittybot2.service.execution;

import android.content.Context;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.pagination.Paginator;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.validation.RuleValidator;
import instantiator.dailykittybot2.validation.ValidationResult;

public class RuleExecutor {
    private static final String TAG = RuleExecutor.class.getName();

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

    private boolean flag_skip_subreddit;
    private boolean flag_finish;
    private boolean flag_cancel;
    private String flagged_subreddit;

    public enum ExecutionMode {
        RespectRuleLastRun(R.string.execution_mode_respect_last_run),
        ActOnLastHourSubmissions(R.string.execution_mode_last_hour),
        ActOnLastDaySubmissions(R.string.execution_mode_last_day),
        ActOnLastWeekSubmissions(R.string.execution_mode_last_week),
        ActOnLastMonthSubmissions(R.string.execution_mode_last_month),
        ActOnLastYearSubmissions(R.string.execution_mode_last_year),
        ActOnAllSubmissions(R.string.execution_mode_all);

        private int text;

        private ExecutionMode(int text_resource) {
            this.text = text_resource;
        }

        public int getText() { return text; }

        @Override
        public String toString() {
            return BotApp.appContext.getString(text);
        }

        public static ExecutionMode[] all() { return ExecutionMode.values(); }

        public static ExecutionMode[] allWithoutRespect() {
            return new ExecutionMode[] {
                    ActOnLastHourSubmissions,
                    ActOnLastDaySubmissions,
                    ActOnLastWeekSubmissions,
                    ActOnLastMonthSubmissions,
                    ActOnLastYearSubmissions,
                    ActOnAllSubmissions
            };
        }


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
        RedditClient reddit = session.getClient();
        String username = reddit.me().getUsername();
        Date start = new Date();

        // find the last considered item for each rule in this subreddit
        Map<UUID, Date> last_considerations = find_last_considered_items(triplets, subreddit);

        // prepare time limits
        List<TimePeriod> periods = determine_best_timeperiods(triplets, last_considerations, mode);
        TimePeriod longest = longest_from(periods);

        // process rules and generate a list of results
        List<RuleResult> out_RuleResults = process_subreddit_incrementally(
                reddit,
                subreddit,
                longest,
                triplets,
                mode,
                last_considerations);

//        List<RuleResult> out_RuleResults = process_everything_at_once(
//                reddit,
//                subreddit,
//                longest,
//                triplets,
//                mode,
//                last_considerations);

        Date finished = new Date();

        // link rule results to reports
        Map<UUID, RunReport> out_RunReports = prepare_blank_run_reports(triplets, username, subreddit);
        compile_rule_results(
                out_RuleResults,
                out_RunReports,
                start,
                finished);

        // final result
        SubredditExecutionResult out_result = new SubredditExecutionResult();
        out_result.subreddit_rule_RunReports = out_RunReports.values();
        out_result.subreddit_rule_RuleResults = out_RuleResults;
        return out_result;
    }

    private List<RuleResult> process_subreddit_incrementally(RedditClient reddit, String subreddit, TimePeriod longest, List<RuleTriplet> triplets, ExecutionMode mode, Map<UUID, Date> last_considerations) {
        List<RuleResult> ruleResults = new LinkedList<>();

        listener.fetching_subreddit_posts(subreddit);
        DefaultPaginator<Submission> paginator = reddit
                .subreddit(subreddit)
                .posts()
                .sorting(SubredditSort.NEW)
                .timePeriod(longest)
                .limit(Paginator.RECOMMENDED_MAX_LIMIT)
                .build();

        int posts = 0;
        for (Listing<Submission> listing : paginator) {
            posts += listing.size();
            listener.testing_subreddit(subreddit, posts);
            for (Submission submission : listing) {
                listener.testing_submission(submission);
                for (RuleTriplet triplet : triplets) {
                    listener.applying_rule(triplet);

                    RuleResult result = execute_rule_on_submission(triplet, submission, mode, longest, last_considerations);
                    ruleResults.add(result);

                    if (flagged_halt_any(subreddit)) { break; }
                } // rule
                if (flagged_halt_any(subreddit)) { break; }
            } // submission
            listener.fetching_subreddit_posts(subreddit);

            if (flagged_halt_any(subreddit)) { break; }
        } // listing

        unset_flags();
        return ruleResults;
    }

    private boolean flagged_halt_any(String current_subreddit) {
        return
                flag_cancel ||
                flag_finish ||
                (flag_skip_subreddit && StringUtils.equals(current_subreddit, flagged_subreddit));
    }

    public void flag_cancel() { flag_cancel = true; }

    public void flag_finish() { flag_finish = true; }

    public void flag_skip_subreddit(String subreddit) {
        flag_skip_subreddit = true;
        flagged_subreddit = subreddit;
    }

    private void unset_flags() {
        flag_cancel = false;
        flag_finish = false;
        flag_skip_subreddit = false;
    }

    private List<RuleResult> process_everything_at_once(RedditClient reddit, String subreddit, TimePeriod longest, List<RuleTriplet> triplets, ExecutionMode mode, Map<UUID, Date> last_considerations) {
        List<RuleResult> ruleResults = new LinkedList<>();

        listener.fetching_subreddit_posts(subreddit);
        List<Submission> submissions = reddit
                .subreddit(subreddit)
                .posts()
                .sorting(SubredditSort.NEW)
                .timePeriod(longest)
                .limit(Paginator.RECOMMENDED_MAX_LIMIT)
                .build()
                .accumulateMerged(-1);

        listener.testing_subreddit(subreddit, submissions.size());

        for (Submission submission : submissions) {
            listener.testing_submission(submission);

            for (RuleTriplet triplet : triplets) {
                listener.applying_rule(triplet);

                RuleResult result = execute_rule_on_submission(triplet, submission, mode, longest, last_considerations);
                ruleResults.add(result);
            } // rule
        } // submission

        return ruleResults;
    }

    private void compile_rule_results(List<RuleResult> out_RuleResults, Map<UUID, RunReport> out_RunReports, Date start, Date finished) {
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

        if (StringUtils.containsIgnoreCase(submission.getTitle(), "google")) {
            Log.v(TAG, "Title contains 'google': " + submission.getTitle());
        }

        if (validation.validates) {

            Date previous_latest =
                    mode == ExecutionMode.RespectRuleLastRun ?
                            last_considerations.get(triplet.rule.uuid) : // respect last run, find last run
                            getDateFromTimePeriod(limit); // get from the time period limit

            if (previous_latest == null) {
                Log.e(TAG, "previous_latest should not be null.");
                Log.e(TAG, "ExecutionMode: " + mode.name());
                Log.e(TAG, "Limit (TimePeriod): " + limit.name());
                previous_latest = getDateFromTimePeriod(TIMEPERIOD_UnhelpfulDefault);
            }

            if (post_is_in_date(submission, previous_latest)) {
                if (rule_matches(triplet, submission)) {
                    result.matched = true;
                    result.recommendations = generate_recommendations(triplet, submission);
                    listener.increment_recommendations(result.recommendations.size());
                    Log.v(TAG, "Accepted by conditions and date. Generated: " + result.recommendations.size());
                } else {
                    Log.v(TAG, "Rejected on conditions.");
                    result.matched = false; // not a good match
                }
            } else {
                Log.v(TAG, "Rejected by date.");
                result.matched = false; // not a good match
            }

            result.ran = true;
        }

        //Log.d(TAG, "Rules rejected by date: " + rejected_by_date);
        //Log.d(TAG, "Rules rejected by conditions: " + rejected_by_rules);

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

    private boolean post_is_in_date(Submission submission, Date latest_previously) {

        boolean is_after_age_cutoff = submission.getCreated().after(latest_previously);

        if (!is_after_age_cutoff) {
            Log.v(TAG, "Submission date: " + submission.getCreated().toGMTString());
            Log.v(TAG, "Cut-off date: " + latest_previously.toGMTString());
        }

        return is_after_age_cutoff;
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
                time = 0;
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

    public static List<TimePeriod> determine_best_timeperiods(List<RuleTriplet> rules, Map<UUID, Date> last_considered_items, ExecutionMode mode) {
        if (mode == ExecutionMode.ActOnLastHourSubmissions) {
            return Arrays.asList(TimePeriod.HOUR);
        }
        if (mode == ExecutionMode.ActOnLastDaySubmissions) {
            return Arrays.asList(TimePeriod.DAY);
        }
        if (mode == ExecutionMode.ActOnLastWeekSubmissions) {
            return Arrays.asList(TimePeriod.WEEK);
        }
        if (mode == ExecutionMode.ActOnLastMonthSubmissions) {
            return Arrays.asList(TimePeriod.MONTH);
        }
        if (mode == ExecutionMode.ActOnLastYearSubmissions) {
            return Arrays.asList(TimePeriod.YEAR);
        }
        if (mode == ExecutionMode.ActOnAllSubmissions) {
            return Arrays.asList(TimePeriod.ALL);
        }

        // mode is RESPECT LAST run...

        List<TimePeriod> list = new LinkedList<>();
        for (RuleTriplet triplet : rules) {
            Date last_run_considered = last_considered_items.get(triplet.rule.uuid);
            if (last_run_considered == null) {
                list.add(TIMEPERIOD_UnhelpfulDefault); // if the rule hasn't run before...
            } else {
                Date now = new Date();
                long diff = now.getTime() - last_run_considered.getTime();
                if (diff < ONE_HOUR) {
                    list.add(TimePeriod.HOUR);
                } else if (diff < ONE_DAY) {
                    list.add(TimePeriod.DAY);
                } else if (diff < ONE_WEEK) {
                    list.add(TimePeriod.WEEK);
                } else if (diff < ONE_MONTH) {
                    list.add(TimePeriod.MONTH);
                } else if (diff < ONE_YEAR) {
                    list.add(TimePeriod.YEAR);
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
        void fetching_subreddit_posts(String subreddit);
        void testing_subreddit(String subreddit, int additional_posts);
        void testing_submission(Submission submission);
        void applying_rule(RuleTriplet rule);
        void increment_recommendations(int recommendations);
    }

}
