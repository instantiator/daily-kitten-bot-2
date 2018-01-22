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

        switch (object.type) {

            case DoNothing:
                warnings.add(context.getString(R.string.validation_outcome_does_nothing));
                break;

//            case UpvotePost:
//            case DownvotePost:
//                warnings.add(context.getString(R.string.validation_upvote_and_downvote_shown_to_fail));
//                break;
        }

        return warnings;
    }
}
