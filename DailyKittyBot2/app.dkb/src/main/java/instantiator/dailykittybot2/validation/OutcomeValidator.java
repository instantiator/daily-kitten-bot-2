package instantiator.dailykittybot2.validation;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.OutcomeType;
import instantiator.dailykittybot2.db.entities.Outcome;

public class OutcomeValidator extends AbstractValidator<Outcome> {


    public OutcomeValidator(Context context) {
        super(context);
    }

    @Override
    protected List<String> check_errors(Outcome object) {
        List<String> errors = new LinkedList<>();

        if (object.type.requiresSpecifics() && StringUtils.isEmpty(object.modifier)) {
            errors.add(context.getString(R.string.validation_outcome_requires_specifics));
        }

        return errors;
    }

    @Override
    protected List<String> check_warnings(Outcome object) {
        List<String> warnings = new LinkedList<>();

        if (object.type == OutcomeType.DoNothing) {
            warnings.add(context.getString(R.string.validation_outcome_does_nothing));
        }

        return warnings;
    }
}
