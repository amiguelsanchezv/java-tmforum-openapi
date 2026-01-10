package com.tmforum.openapi.validation;

import com.tmforum.openapi.repository.CustomerRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator to verify that an email is unique in the database
 */
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // @NotNull/@NotBlank validation handles this
        }
        
        return !customerRepository.existsByEmail(email);
    }
}


