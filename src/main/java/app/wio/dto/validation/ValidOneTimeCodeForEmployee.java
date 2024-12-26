package app.wio.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneTimeCodeForEmployeeValidator.class)
@Documented
public @interface ValidOneTimeCodeForEmployee {
    String message() default "One-time code is required for EMPLOYEE registrations.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
