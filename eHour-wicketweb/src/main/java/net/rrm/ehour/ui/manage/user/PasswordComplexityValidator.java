package net.rrm.ehour.ui.manage.user;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.PatternValidator;

/**
 * Validator for password complexity
 */
public class PasswordComplexityValidator extends PatternValidator {

    private static final long serialVersionUID = 1L;
    private static final PasswordComplexityValidator INSTANCE = new PasswordComplexityValidator();

    public static PasswordComplexityValidator getInstance() {
        return INSTANCE;
    }

    protected PasswordComplexityValidator() {
        //super("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        super("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[~!@#$%^&+=])(?=\\S+$).{8,}$");
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        String password = validatable.getValue();

        if (password != null && password.length() > 0 && !this.getPattern().matcher((CharSequence)password).matches()) {
            // FIXME: use resource file
            validatable.error(new ValidationError("Password must contain at least 1 digit (0-9), 1 alphabet (a-z, A-Z) and 1 special character (~!@#$%^&+=). No spaces allowed. Min length is 8"));
        }
    }
}
