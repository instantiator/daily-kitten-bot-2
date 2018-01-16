package instatiator.dailykittybot2.service.execution;

import android.content.Context;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.service.RedditSession;
import instatiator.dailykittybot2.validation.RuleValidator;
import instatiator.dailykittybot2.validation.ValidationResult;

public class RuleExecutor {
    public static final long ONE_HOUR = 1000 * 60 * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;
    public static final long ONE_WEEK = ONE_DAY * 7;
    public static final long ONE_MONTH = ONE_WEEK * 4;

    private Context context;
    private RedditSession session;
    private Listener listener;
    private ConditionMatcher matcher;
    private RecommendationGenerator generator;

    public enum ExecutionMode {
        RespectRuleLastRun,
        ActOnLastHourSubmissions,
        ActOnLastDaySubmissions
    }

    public RuleExecutor(Context context, RedditSession session, Listener listener) {
        this.session = session;
        this.listener = listener;
        this.context = context;

        this.matcher = new ConditionMatcher(session.getClient());
        this.generator = new RecommendationGenerator();
    }

    public List<RuleResult> execute_rules_for_subreddit(String subreddit, List<RuleTriplet> rules, ExecutionMode mode) {
        listener.testing_subreddit(subreddit);

        List<RuleResult> results = new LinkedList<>();
        RedditClient reddit = session.getClient();

        SubredditReference subreddit_ref = reddit.subreddit(subreddit);

        List<TimePeriod> periods = determine_best_timeperiods(rules, mode);
        TimePeriod longest = longest_from(periods);

        DefaultPaginator<Submission> paginator =
                subreddit_ref
                        .posts()
                        .timePeriod(longest)
                        .sorting(SubredditSort.NEW)
                        .build();

        for (Listing<Submission> page : paginator) {
            for (Submission submission : page.getChildren()) {
                listener.testing_submission(submission);

                for (RuleTriplet rule : rules) {
                    listener.applying_rule(rule);

                    RuleResult result = new RuleResult();
                    result.rule = rule;

                    RuleValidator validator = new RuleValidator(context);
                    ValidationResult validation = validator.validate(rule);
                    result.validates = validation.validates;
                    result.errors = validation.errors;
                    result.warnings = validation.warnings;

                    if (validation.validates) {
                        if (rule_matches(rule, submission)) {
                            result.matched = true;
                            result.recommendations = generate_recommendations(rule, submission);
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
        List<TimePeriod> list = new LinkedList<>();
        for (RuleTriplet rule : rules) {
            if (rule.rule.last_run == null) {
                list.add(TimePeriod.DAY); // falls back on the last 24 hours
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
