package instantiator.dailykittybot2.service.helpers;

import android.content.Context;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.ConditionType;
import instantiator.dailykittybot2.data.OutcomeType;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.BotDatabase;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Rule;

public class SampleDataInjector {

    private static int[] sample_data_rule_names = new int[] {
            R.string.sample_rule_match_colours,
            R.string.sample_rule_find_tomato,
            R.string.sample_rule_upvote_all_bbc_links
    };

    private BotDatabase db;
    private Context context;
    private String username;

    public static List<String> get_rule_names(Context context) {
        List<String> names = new LinkedList<>();
        for (int res : sample_data_rule_names) {
            names.add(context.getString(res));
        }
        return names;
    }

    public SampleDataInjector(Context context, String username, BotDatabase db) {
        this.context = context;
        this.username = username;
        this.db = db;
    }

    private int find_name(String content) {
        for (int res : sample_data_rule_names) {
            if (context.getString(res).equals(content)) {
                return res;
            }
        }
        throw new IllegalArgumentException();
    }

    public RuleTriplet inject(String name_str) {
        int name = find_name(name_str);

        if (!ArrayUtils.contains(sample_data_rule_names, name)) { return null; }

        RuleTriplet created = new RuleTriplet();
        created.rule = create_rule(name);
        created.conditions = create_conditions(name, created.rule.uuid);
        created.outcomes = create_outcomes(name, created.rule.uuid);

        db.ruleDao().insertAll(created.rule);
        db.conditionDao().insertAll(created.conditions.toArray(new Condition[created.conditions.size()]));
        db.outcomeDao().insertAll(created.outcomes.toArray(new Outcome[created.outcomes.size()]));

        return created;
    }

    public Rule create_rule(int name) {
        Rule rule = new Rule();
        rule.uuid = UUID.randomUUID();
        rule.username = username;
        rule.rulename = context.getString(name);
        rule.subreddits = Arrays.asList(get_subreddits(name));
        rule.username = username;
        return rule;
    }

    private String[] get_subreddits(int name) {
        switch (name) {

            case R.string.sample_rule_match_colours:
                return context.getResources().getStringArray(R.array.sample_rule_subreddits_dkb);

            case R.string.sample_rule_find_tomato:
                return context.getResources().getStringArray(R.array.sample_rule_subreddits_dkb);

            case R.string.sample_rule_upvote_all_bbc_links:
                return context.getResources().getStringArray(R.array.sample_rule_subreddits_dkb);

            default:
                return null;
        }
    }

    public List<Condition> create_conditions(int name, UUID rule) {
        List<Condition> conditions = new LinkedList<>();
        switch (name) {
            case R.string.sample_rule_upvote_all_bbc_links:
                Condition condition_bbc = new Condition();
                condition_bbc.uuid = UUID.randomUUID();
                condition_bbc.ruleUuid = rule;
                condition_bbc.ordering = 0;
                condition_bbc.type = ConditionType.IfIsLinkForAnyDomainsOf;
                condition_bbc.modifier = context.getString(R.string.sample_rule_modifier_all_bbc_links);
                conditions.add(condition_bbc);
                break;

            case R.string.sample_rule_match_colours:
                Condition condition_rainbow = new Condition();
                condition_rainbow.uuid = UUID.randomUUID();
                condition_rainbow.ruleUuid = rule;
                condition_rainbow.ordering = 0;
                condition_rainbow.type = ConditionType.IfTextContainsWordsFrom;
                condition_rainbow.modifier = context.getString(R.string.sample_rule_modifier_colours);
                conditions.add(condition_rainbow);
                break;

            case R.string.sample_rule_find_tomato:
                Condition condition_tomato = new Condition();
                condition_tomato.uuid = UUID.randomUUID();
                condition_tomato.ruleUuid = rule;
                condition_tomato.ordering = 0;
                condition_tomato.type = ConditionType.IfTextContainsString;
                condition_tomato.modifier = context.getString(R.string.sample_rule_modifier_tomato);
                conditions.add(condition_tomato);
                break;
        }
        return conditions;
    }

    public List<Outcome> create_outcomes(int name, UUID rule) {
        switch (name) {
            case R.string.sample_rule_upvote_all_bbc_links:
                return create_upvote_outcome(name, rule);

            default:
                return create_comment_outcome(name, rule);
        }
    }

    public List<Outcome> create_do_nothing_outcome(int name, UUID rule) {
        List<Outcome> outcomes = new LinkedList<>();

        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.ruleUuid = rule;
        outcome.ordering = 0;
        outcome.type = OutcomeType.DoNothing;
        outcomes.add(outcome);

        return outcomes;
    }

    public List<Outcome> create_upvote_outcome(int name, UUID rule) {
        List<Outcome> outcomes = new LinkedList<>();

        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.ruleUuid = rule;
        outcome.ordering = 0;
        outcome.type = OutcomeType.UpvotePost;
        outcomes.add(outcome);

        return outcomes;
    }

    public List<Outcome> create_comment_outcome(int name, UUID rule) {
        List<Outcome> outcomes = new LinkedList<>();

        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.ruleUuid = rule;
        outcome.ordering = 0;
        outcome.type = OutcomeType.AddCommentToPost;
        outcome.modifier = context.getString(R.string.sample_comment_generic);
        outcomes.add(outcome);

        return outcomes;
    }

}
