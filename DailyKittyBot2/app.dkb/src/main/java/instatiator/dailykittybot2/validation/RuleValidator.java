package instatiator.dailykittybot2.validation;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.ConditionType;
import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;

public class RuleValidator extends AbstractValidator<RuleTriplet> {

    private ConditionValidator conditionValidator;
    private OutcomeValidator outcomeValidator;

    public RuleValidator(Context context) {
        super(context);
        conditionValidator = new ConditionValidator(context);
        outcomeValidator = new OutcomeValidator(context);
    }

    @Override
    protected List<String> check_errors(RuleTriplet triplet) {
        List<String> errors = new LinkedList<>();

        if (triplet.rule == null) {
            errors.add(context.getString(R.string.validation_rule_is_null));
            return errors;
        }

        if (triplet.rule.subreddits.size() == 0) {
            errors.add(context.getString(R.string.validation_rule_has_no_subreddits));
        }

        if (triplet.conditions != null && triplet.conditions.size() > 0) {
            Condition c = findFirstRunning(triplet.conditions, true);
            if (c != null && !c.type.isPostFilter()) {
                errors.add(context.getString(R.string.validation_first_condition_must_filter_posts));
            }
        }

        int condition_errors = 0;
        if (triplet.conditions != null) {
            for (Condition condition : triplet.conditions) {
                ValidationResult result = conditionValidator.validate(condition);
                if (result.errors.size() > 0) { condition_errors++; }
            }
        }

        int outcome_errors = 0;
        if (triplet.outcomes != null) {
            for (Outcome outcome : triplet.outcomes) {
                ValidationResult result = outcomeValidator.validate(outcome);
                if (result.errors.size() > 0) { outcome_errors++; }
            }
        }

        if (condition_errors > 0) {
            errors.add(context.getString(R.string.validation_rule_has_X_condition_errors, condition_errors));
        }

        if (outcome_errors > 0) {
            errors.add(context.getString(R.string.validation_rule_has_X_outcome_errors, outcome_errors));
        }

        return errors;
    }

    private Condition findFirstRunning(List<Condition> conditions, boolean ignoreNeverMatch) {
        int max_order = 0;
        Condition found = null;
        for (Condition c : conditions) {
            if (ignoreNeverMatch && c.type == ConditionType.NeverMatch) {
                continue;
            }

            if (found == null || c.ordering > max_order) {
                found = c;
                max_order = c.ordering;
            }
        }
        return found;
    }

    @Override
    protected List<String> check_warnings(RuleTriplet triplet) {
        List<String> warnings = new LinkedList<>();

        if (triplet.rule == null) {
            return warnings; // do not process - a serious lack tasks_of rule has already occurred!
        }

        if (StringUtils.isBlank(triplet.rule.rulename)) {
            warnings.add(context.getString(R.string.validation_rule_has_blank_name));
        }

        int condition_warnings = 0;
        if (triplet.conditions != null) {
            for (Condition condition : triplet.conditions) {
                ValidationResult result = conditionValidator.validate(condition);
                if (result.warnings.size() > 0) { condition_warnings++; }
            }

            if (triplet.conditions.size() == 0) {
                warnings.add(context.getString(R.string.validation_rule_has_no_conditions));
            }
        }

        int outcome_warnings = 0;
        if (triplet.outcomes != null) {
            for (Outcome outcome : triplet.outcomes) {
                ValidationResult result = outcomeValidator.validate(outcome);
                if (result.warnings.size() > 0) { outcome_warnings++; }
            }

            if (triplet.outcomes.size() == 0) {
                warnings.add(context.getString(R.string.validation_rule_has_no_outcomes));
            }
        }

        if (condition_warnings > 0) {
            warnings.add(context.getString(R.string.validation_rule_has_X_condition_warnings, condition_warnings));
        }

        if (outcome_warnings > 0) {
            warnings.add(context.getString(R.string.validation_rule_has_X_outcome_warnings, outcome_warnings));
        }

        return warnings;
    }
}
