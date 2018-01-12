package instatiator.dailykittybot2.validation;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.ConditionType;
import instatiator.dailykittybot2.db.entities.Condition;

public class ConditionValidator extends AbstractValidator<Condition> {

    public ConditionValidator(Context context, Condition item) {
        super(context, item);
    }

    @Override
    protected List<String> check_errors(Condition object) {
        List<String> errors = new LinkedList<>();
        return errors;
    }

    @Override
    protected List<String> check_warnings(Condition object) {
        List<String> warnings = new LinkedList<>();

        if (object.type == ConditionType.NothingSelected) {
            warnings.add(context.getString(R.string.validation_condition_does_nothing));
        }

        return warnings;
    }
}
