package com.tmforum.openapi.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidPhoneNumberValidatorTest {
    
    private ValidPhoneNumberValidator validator;
    
    @Mock
    private ValidPhoneNumber constraintAnnotation;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @BeforeEach
    void setUp() {
        validator = new ValidPhoneNumberValidator();
        validator.initialize(constraintAnnotation);
    }
    
    @Test
    void testIsValid_NullPhoneNumber_ReturnsTrue() {
        // The validation of @NotNull/@NotBlank handles this
        assertTrue(validator.isValid(null, context));
    }
    
    @Test
    void testIsValid_EmptyPhoneNumber_ReturnsTrue() {
        // The validation of @NotNull/@NotBlank handles this
        assertTrue(validator.isValid("", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_7Digits_ReturnsTrue() {
        assertTrue(validator.isValid("1234567", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_10Digits_ReturnsTrue() {
        assertTrue(validator.isValid("1234567890", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_15Digits_ReturnsTrue() {
        assertTrue(validator.isValid("123456789012345", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_TooShort_ReturnsFalse() {
        // Less than 7 digits
        assertFalse(validator.isValid("123456", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_TooLong_ReturnsFalse() {
        // More than 15 digits
        assertFalse(validator.isValid("1234567890123456", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_WithLetters_ReturnsFalse() {
        assertFalse(validator.isValid("1234567a", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_WithSpaces_ReturnsFalse() {
        assertFalse(validator.isValid("123 4567", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_WithDashes_ReturnsFalse() {
        assertFalse(validator.isValid("123-4567", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_WithParentheses_ReturnsFalse() {
        assertFalse(validator.isValid("(123)4567", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_WithPlusSign_ReturnsFalse() {
        assertFalse(validator.isValid("+1234567", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_8Digits_ReturnsTrue() {
        assertTrue(validator.isValid("12345678", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_9Digits_ReturnsTrue() {
        assertTrue(validator.isValid("123456789", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_11Digits_ReturnsTrue() {
        assertTrue(validator.isValid("12345678901", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_12Digits_ReturnsTrue() {
        assertTrue(validator.isValid("123456789012", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_13Digits_ReturnsTrue() {
        assertTrue(validator.isValid("1234567890123", context));
    }
    
    @Test
    void testIsValid_ValidPhoneNumber_14Digits_ReturnsTrue() {
        assertTrue(validator.isValid("12345678901234", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_OnlyLetters_ReturnsFalse() {
        assertFalse(validator.isValid("abcdefg", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_MixedChars_ReturnsFalse() {
        assertFalse(validator.isValid("1234abc", context));
    }
    
    @Test
    void testIsValid_InvalidPhoneNumber_WithSpecialChars_ReturnsFalse() {
        assertFalse(validator.isValid("1234@567", context));
    }
    
    @Test
    void testInitialize_DoesNotThrow() {
        // Verify that initialize does not throw exceptions
        assertDoesNotThrow(() -> validator.initialize(constraintAnnotation));
    }
}
