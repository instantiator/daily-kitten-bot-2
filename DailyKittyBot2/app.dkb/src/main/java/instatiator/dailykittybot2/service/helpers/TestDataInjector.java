package instatiator.dailykittybot2.service.helpers;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import instatiator.dailykittybot2.db.BotDatabase;
import instatiator.dailykittybot2.data.ConditionType;
import instatiator.dailykittybot2.data.OutcomeType;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;
import main.java.com.maximeroussy.invitrode.RandomWord;
import main.java.com.maximeroussy.invitrode.WordLengthException;

public class TestDataInjector {
    private static final String TAG = TestDataInjector.class.getName();
    private static final int LEN_RULE_NAME = 8;

    private static final int LEN_CONDITION_MODIFIER = 11;
    private static final int LEN_OUTCOME_MODIFIER = 11;

    private BotDatabase db;
    private String username;

    public TestDataInjector(BotDatabase db, String username) {
        this.db = db;
        this.username = username;
    }

    public void inject_full_test_rule(int with_conditions, int with_outcomes) {
        Rule rule = create_rule();

        List<Condition> conditions = new LinkedList<>();
        List<Outcome> outcomes = new LinkedList<>();

        for (int i = 0; i < with_conditions; i++)
            conditions.add(create_condition(rule.uuid, i));

        for (int i = 0; i < with_outcomes; i++)
            outcomes.add(create_outcome(rule.uuid));

        db.ruleDao().insertAll(rule);
        db.conditionDao().insertAll(conditions.toArray(new Condition[conditions.size()]));
        db.outcomeDao().insertAll(outcomes.toArray(new Outcome[outcomes.size()]));
    }

    private Rule create_rule() {
        Rule rule = new Rule();
        rule.uuid = UUID.randomUUID();
        rule.username = username;
        rule.rulename = generate_word(LEN_RULE_NAME, "New rule");
        return rule;
    }

    private Condition create_condition(UUID rule, int ordering) {
        Condition condition = new Condition();
        condition.uuid = UUID.randomUUID();
        condition.ruleUuid = rule;
        condition.type = choose_random(ConditionType.values());
        condition.modifier = generate_word(LEN_CONDITION_MODIFIER, "Condition modifier");
        condition.ordering = ordering;
        return condition;
    }

    private Outcome create_outcome(UUID rule) {
        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.ruleUuid = rule;
        outcome.type = choose_random(OutcomeType.values());
        outcome.modifier = generate_word(LEN_OUTCOME_MODIFIER, "Outcome modifier");
        return outcome;
    }

    private static Random random = new Random();
    public static<T extends Enum<T>> T choose_random(T[] values) {
        return values[random.nextInt(values.length)];
    }

    public static String generate_word(int length, String fallback) {
        try {
            return RandomWord.getNewWord(length);
        } catch (WordLengthException e) {
            Log.w(TAG, "Somehow, a word length exception.");
            return fallback;
        }
    }
}
