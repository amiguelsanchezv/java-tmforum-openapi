package com.tmforum.openapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation to verify phone number format
 */
@Documented
@Constraint(validatedBy = ValidPhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "The phone number format is not valid (must contain only numbers and have between 7 and 15 digits)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


