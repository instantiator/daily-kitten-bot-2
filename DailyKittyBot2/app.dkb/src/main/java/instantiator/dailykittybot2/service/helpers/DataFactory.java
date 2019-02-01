package instantiator.dailykittybot2.service.helpers;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.data.ConditionType;
import instantiator.dailykittybot2.data.OutcomeType;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Rule;

public class DataFactory {

    public static Rule create_rule(String username, String rulename) {
        Rule rule = new Rule();
        rule.uuid = UUID.randomUUID();
        rule.username = username;
        rule.rulename = rulename;
        return rule;
    }

    public static Condition create_condition(UUID rule_id, int ordering) {
        Condition condition = new Condition();
        condition.uuid = UUID.randomUUID();
        condition.type = ConditionType.NeverMatch;
        condition.ruleUuid = rule_id;
        condition.ordering = ordering;
        return condition;
    }

    public static Outcome create_outcome(UUID rule_id) {
        Outcome outcome = new Outcome();
        outcome.uuid = UUID.randomUUID();
        outcome.type = OutcomeType.DoNothing;
        outcome.ruleUuid = rule_id;
        return outcome;
    }

    public static Enaction create_enaction(String username, UUID recommendation_id) {
        Enaction enaction = new Enaction();
        enaction.uuid = UUID.randomUUID();
        enaction.username = username;
        enaction.recommendationUuid = recommendation_id;
        enaction.errors = new LinkedList<>();
        return enaction;
    }

}
