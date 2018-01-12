package instatiator.dailykittybot2.service.validation;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;

public class RuleValidator extends AbstractValidator<Rule> {

    public RuleValidator(Context context, Rule item) {
        super(context, item);
    }

    @Override
    protected List<String> check_errors(Rule object) {
        List<String> errors = new LinkedList<>();

        if (object.subreddits.size() == 0) {
            errors.add(context.getString(R.string.validation_rule_has_no_subreddits));
        }

        return errors;
    }

    @Override
    protected List<String> check_warnings(Rule object) {
        List<String> warnings = new LinkedList<>();

        if (StringUtils.isBlank(object.rulename)) {
            warnings.add(context.getString(R.string.validation_rule_has_blank_name));
        }

        return warnings;
    }
}
