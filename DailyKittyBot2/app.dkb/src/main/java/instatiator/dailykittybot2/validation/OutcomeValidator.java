package instatiator.dailykittybot2.validation;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.OutcomeType;
import instatiator.dailykittybot2.db.entities.Outcome;

public class OutcomeValidator extends AbstractValidator<Outcome> {


    public OutcomeValidator(Context context) {
        super(context);
    }

    @Override
    protected List<String> check_errors(Outcome object) {
        List<String> errors = new LinkedList<>();
        return errors;
    }

    @Override
    protected List<String> check_warnings(Outcome object) {
        List<String> warnings = new LinkedList<>();

        if (object.type == OutcomeType.NothingSelected) {
            warnings.add(context.getString(R.string.validation_outcome_does_nothing));
        }

        return warnings;
    }
}
