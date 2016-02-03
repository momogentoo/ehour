package net.rrm.ehour.ui.manage.user;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.PatternValidator;

/**
 * Validator for 2-digit country code of a user
 */
public class CountryCodeValidator extends PatternValidator {

    private static final long serialVersionUID = 1L;
    private static final CountryCodeValidator INSTANCE = new CountryCodeValidator();

    public static CountryCodeValidator getInstance() {
        return INSTANCE;
    }

    protected CountryCodeValidator() {
        super("^[A-Z]{2}$");
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        String countryCode = validatable.getValue();

        if (countryCode == null || countryCode.length() == 0 || !this.getPattern().matcher((CharSequence)countryCode).matches()) {
            // FIXME: use resource file
            validatable.error(new ValidationError("Country Code must be 2-digit and cannot be null"));
        }
    }
}
