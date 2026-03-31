package galindo.raul.virtualclubs.models.annotations;

import galindo.raul.virtualclubs.models.annotations.validators.StrongPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation to validate that a password meets the strength requirements.
 * The validation logic is defined in {@link StrongPasswordValidator}.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
  
  /**
   * Error message to be returned when validation fails.
   * @return the error message or key.
   */
  String message() default "PASSWORD_TOO_WEAK";
  
  /**
   * Groups for which this constraint is applicable.
   * @return the groups.
   */
  Class<?>[] groups() default {};
  
  /**
   * Payload that can be used by clients of the Jakarta Bean Validation API.
   * @return the payload.
   */
  Class<? extends Payload>[] payload() default {};
}
