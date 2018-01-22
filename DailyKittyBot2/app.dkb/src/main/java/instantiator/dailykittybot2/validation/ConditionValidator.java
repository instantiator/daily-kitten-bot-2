package instantiator.dailykittybot2.validation;

import android.content.Context;
import android.net.Uri;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.ConditionType;
import instantiator.dailykittybot2.db.entities.Condition;

public class ConditionValidator extends AbstractValidator<Condition> {

    public ConditionValidator(Context context) {
        super(context);
    }

    @Override
    protected List<String> check_errors(Condition object) {
        List<String> errors = new LinkedList<>();

        if (object.type.requiresSpecifics()) {
            if (StringUtils.isEmpty(object.modifier)) {
                errors.add(context.getString(R.string.validation_condition_requires_specifics));
            } else {
                int min_match_length = context.getResources().getInteger(R.integer.validation_min_match_size);
                if (object.modifier.length() < min_match_length) {
                    errors.add(context.getString(R.string.validation_condition_specifics_too_short_min, min_match_length));
                }
            }
        }

        switch (object.type) {
            case IfIsExactLink:
            case IfAnyCommentContainsLink:
            case IfNoCommentContainsLink:
            case IfTextContainsLink:
                try {
                    Uri found = Uri.parse(object.modifier);
                    if (found == null) {
                        errors.add(context.getString(R.string.validation_cannot_parse_uri));
                    }
                } catch (Exception e) {
                    errors.add(context.getString(R.string.validation_cannot_parse_uri));
                }
                break;
        }

        return errors;
    }

    @Override
    protected List<String> check_warnings(Condition object) {
        List<String> warnings = new LinkedList<>();

        if (object.type == ConditionType.NeverMatch) {
            warnings.add(context.getString(R.string.validation_condition_does_nothing));
        }

        return warnings;
    }
}
