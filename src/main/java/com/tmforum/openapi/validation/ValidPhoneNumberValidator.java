package com.tmforum.openapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator to verify phone number format
 */
public class ValidPhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^[0-9]{7,15}$");
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true; // @NotNull/@NotBlank validation handles this
        }
        
        return PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }
}


