package net.rrm.ehour.ui.userprefs.panel;

import net.rrm.ehour.ui.common.component.AjaxFormComponentFeedbackIndicator;
import net.rrm.ehour.ui.manage.user.PasswordComplexityValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

public class PasswordFieldFactory {
    public static void createOptionalPasswordFields(Form<?> form, IModel<String> model) {
        createPasswordFields(form, model, true);
    }

    public static void createPasswordFields(Form<?> form, IModel<String> model) {
        createPasswordFields(form, model, false);
    }

    private static void createPasswordFields(Form<?> form, IModel<String> model, boolean optional) {
        // password inputs
        PasswordTextField passwordTextField = new PasswordTextField("password", model);
        passwordTextField.setLabel(new ResourceModel("user.password"));
        passwordTextField.setRequired(!optional);
        passwordTextField.add(PasswordComplexityValidator.getInstance());
        form.add(passwordTextField);

        PasswordTextField confirmPasswordTextField = new PasswordTextField("confirmPassword", new Model<String>());
        confirmPasswordTextField.setRequired(!optional);
        confirmPasswordTextField.add(PasswordComplexityValidator.getInstance());

        form.add(confirmPasswordTextField);
        form.add(new AjaxFormComponentFeedbackIndicator("confirmPasswordValidationError", confirmPasswordTextField));
        form.add(new EqualPasswordInputValidator(passwordTextField, confirmPasswordTextField) {
            protected String resourceKey() {
                return "user.errorConfirmPassNeeded";
            }
        });
    }
}
