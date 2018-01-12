package instatiator.dailykittybot2.service.validation;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractValidator<T> implements IEntityValidator {

    protected Context context;
    protected T item;

    protected AbstractValidator(Context context, T item) {
        this.item = item;
        this.context = context;
    }

    public T getItem() { return item; }
    public void setItem(T item) { this.item = item; }

    @Override
    public ValidationResult validate() {
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
