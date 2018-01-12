package instatiator.dailykittybot2.validation;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractValidator<T> {

    protected Context context;

    protected AbstractValidator(Context context) {
        this.context = context;
    }

    public ValidationResult validate(T item) {
        List<String> errors = new LinkedList<>();
        List<String> warnings = new LinkedList<>();
        errors.addAll(check_errors(item));
        warnings.addAll(check_warnings(item));
        boolean ok = errors.size() == 0;
        return new ValidationResult(ok, errors, warnings);
    }

    protected abstract List<String> check_errors(T object);
    protected abstract List<String> check_warnings(T object);
}
