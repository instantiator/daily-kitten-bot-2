package instatiator.dailykittybot2.service.helpers;

import java.util.UUID;

import instatiator.dailykittybot2.db.data.ConditionType;
import instatiator.dailykittybot2.db.data.OutcomeType;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;

public class DataFactory {

    public static Rule create_rule(String username, String rulename) {
        Rule rule = new Rule();
        rule.uuid = UUID.randomUUID();
        rule.username = username;
        rule.rulename = rulename;
        return rule;
    }

    public static Condition create_condition(UUID rule_id) {
        Condition condition = new Condition();
        condition.uuid = UUID.randomUUID();
        condition.type = ConditionType.NothingSelected;
        condition.ruleUuid = rule_id;
        return condition;
    }

    public static Outcome create_outcome(UUID rule_id) {
        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.type = OutcomeType.NothingSelected;
        outcome.ruleUuid = rule_id;
        return outcome;
    }

}
