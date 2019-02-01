package instantiator.dailykittybot2.validation;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import instantiator.dailykittybot2.R;

public abstract class AbstractValidator<T> {

    protected Context context;

    protected AbstractValidator(Context context) {
        this.context = context;
    }

    public ValidationResult validate(T item) {
        List<String> errors = new LinkedList<>();
        List<String> warnings = new LinkedList<>();

        if (item == null) {
            errors.add(context.getString(R.string.validation_item_is_null));
            return new ValidationResult(false, errors, warnings);
        }

        errors.addAll(check_errors(item));
        warnings.addAll(check_warnings(item));

        boolean ok = errors.size() == 0;
        return new ValidationResult(ok, errors, warnings);
    }

    protected abstract List<String> check_errors(T object);
    protected abstract List<String> check_warnings(T object);
}
