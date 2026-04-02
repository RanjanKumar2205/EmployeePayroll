package org.eample.employeepayoll.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContactNumber {
    String message() default "Invalid contact number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
