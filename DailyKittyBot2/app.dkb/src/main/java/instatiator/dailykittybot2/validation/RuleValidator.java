package instatiator.dailykittybot2.validation;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.LinkedList;
import java.util.List;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;

public class RuleValidator extends AbstractValidator<ImmutableTriple<Rule,List<Condition>, List<Outcome>>> {

    private ConditionValidator conditionValidator;
    private OutcomeValidator outcomeValidator;

    public RuleValidator(Context context) {
        super(context);
        conditionValidator = new ConditionValidator(context);
        outcomeValidator = new OutcomeValidator(context);
    }

    @Override
    protected List<String> check_errors(ImmutableTriple<Rule,List<Condition>, List<Outcome>> subject) {
        List<String> errors = new LinkedList<>();

        if (subject.getLeft().subreddits.size() == 0) {
            errors.add(context.getString(R.string.validation_rule_has_no_subreddits));
        }

        int condition_errors = 0;
        if (subject.getMiddle() != null) {
            for (Condition condition : subject.getMiddle()) {
                ValidationResult result = conditionValidator.validate(condition);
                if (result.errors.size() > 0) { condition_errors++; }
            }
        }

        int outcome_errors = 0;
        if (subject.getRight() != null) {
            for (Outcome outcome : subject.getRight()) {
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

    @Override
    protected List<String> check_warnings(ImmutableTriple<Rule,List<Condition>, List<Outcome>> subject) {
        List<String> warnings = new LinkedList<>();

        if (StringUtils.isBlank(subject.getLeft().rulename)) {
            warnings.add(context.getString(R.string.validation_rule_has_blank_name));
        }

        int condition_warnings = 0;
        if (subject.getMiddle() != null) {
            for (Condition condition : subject.getMiddle()) {
                ValidationResult result = conditionValidator.validate(condition);
                if (result.warnings.size() > 0) { condition_warnings++; }
            }

            if (subject.getMiddle().size() == 0) {
                warnings.add(context.getString(R.string.validation_rule_has_no_conditions));
            }
        }

        int outcome_warnings = 0;
        if (subject.getRight() != null) {
            for (Outcome outcome : subject.getRight()) {
                ValidationResult result = outcomeValidator.validate(outcome);
                if (result.warnings.size() > 0) { outcome_warnings++; }
            }

            if (subject.getRight().size() == 0) {
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
