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
            R.string.sample_rule_weekly_androiddev,
            R.string.sample_rule_google_androiddev
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
        rule.subreddits = get_subreddits(name);
        rule.username = username;
        return rule;
    }

    private List<String> get_subreddits(int name) {
        switch (name) {
            case R.string.sample_rule_google_androiddev:
            case R.string.sample_rule_weekly_androiddev:
                return Arrays.asList("androiddev");
            default:
                return null;
        }
    }

    public List<Condition> create_conditions(int name, UUID rule) {
        List<Condition> conditions = new LinkedList<>();
        switch (name) {
            case R.string.sample_rule_google_androiddev:
                Condition condition_g = new Condition();
                condition_g.uuid = UUID.randomUUID();
                condition_g.ruleUuid = rule;
                condition_g.ordering = 0;
                condition_g.type = ConditionType.IfTitleContainsWordsFrom;
                condition_g.modifier = "google";
                conditions.add(condition_g);
                break;
            case R.string.sample_rule_weekly_androiddev:
                Condition condition_w = new Condition();
                condition_w.uuid = UUID.randomUUID();
                condition_w.ruleUuid = rule;
                condition_w.ordering = 0;
                condition_w.type = ConditionType.IfTitleContainsWordsFrom;
                condition_w.modifier = "weekly";
                conditions.add(condition_w);
                break;
        }
        return conditions;
    }

    public List<Outcome> create_outcomes(int name, UUID rule) {
        List<Outcome> outcomes = new LinkedList<>();

        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.ruleUuid = rule;
        outcome.ordering = 0;
        outcome.type = OutcomeType.UpvotePost;
        outcomes.add(outcome);

        return outcomes;
    }

}
