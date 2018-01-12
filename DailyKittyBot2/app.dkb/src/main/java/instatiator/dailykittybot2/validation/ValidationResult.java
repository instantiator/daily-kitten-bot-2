package instatiator.dailykittybot2.validation;

import java.util.LinkedList;
import java.util.List;

public class ValidationResult {

    public ValidationResult(boolean ok, List<String> errors, List<String> warnings) {
        this.validates = ok;
        this.errors = errors == null ? new LinkedList<>() : errors;
        this.warnings = warnings == null ? new LinkedList<>() : warnings;
    }

    public boolean validates;
    public List<String> errors;
    public List<String> warnings;
}
