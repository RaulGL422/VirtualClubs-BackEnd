package galindo.raul.virtualclubs.models.annotations.validators;

import galindo.raul.virtualclubs.models.annotations.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the {@link StrongPassword} annotation.
 * Validates that a string meets specific complexity requirements.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

  /**
   * Regular expression for password validation.
   * Requires at least 1 letter, 1 digit, and a minimum length of 6 characters.
   */
  private static final String PASSWORD_REGEX =
      "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$";
  
  /**
   * Validates the given password against the requirements.
   *
   * @param password the password string to validate.
   * @param context  context in which the constraint is evaluated.
   * @return true if the password matches the regex and is not null, false otherwise.
   */
  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    
    if (password == null) {
      return false;
    }
    
    return password.matches(PASSWORD_REGEX);
  }
}