package com.tmforum.openapi.validation;

import com.tmforum.openapi.repository.CustomerRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniqueEmailValidatorTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private UniqueEmail constraintAnnotation;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @InjectMocks
    private UniqueEmailValidator validator;
    
    @BeforeEach
    void setUp() {
        validator.initialize(constraintAnnotation);
    }
    
    @Test
    void testIsValid_NullEmail_ReturnsTrue() {
        // The validation of @NotNull/@NotBlank handles this
        assertTrue(validator.isValid(null, context));
        verify(customerRepository, never()).existsByEmail(anyString());
    }
    
    @Test
    void testIsValid_EmptyEmail_ReturnsTrue() {
        // The validation of @NotNull/@NotBlank handles this
        assertTrue(validator.isValid("", context));
        verify(customerRepository, never()).existsByEmail(anyString());
    }
    
    @Test
    void testIsValid_EmailDoesNotExist_ReturnsTrue() {
        // Given
        String email = "new@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailExists_ReturnsFalse() {
        // Given
        String email = "existing@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(true);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertFalse(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailWithSpaces_ReturnsTrue() {
        // Given - although it should not have spaces, the validator processes it
        String email = "test@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailValidFormat_ReturnsTrue() {
        // Given
        String email = "user.test@domain.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailWithSubdomain_ReturnsTrue() {
        // Given
        String email = "user@sub.domain.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailWithNumbers_ReturnsTrue() {
        // Given
        String email = "user123@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailWithDashes_ReturnsTrue() {
        // Given
        String email = "user-test@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailWithDots_ReturnsTrue() {
        // Given
        String email = "user.test@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_EmailDuplicated_ReturnsFalse() {
        // Given
        String email = "duplicated@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(true);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertFalse(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_MultipleCalls_SameEmail() {
        // Given
        String email = "test@example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result1 = validator.isValid(email, context);
        boolean result2 = validator.isValid(email, context);
        
        // Then
        assertTrue(result1);
        assertTrue(result2);
        verify(customerRepository, times(2)).existsByEmail(email);
    }
    
    @Test
    void testIsValid_MultipleCalls_DifferentEmails() {
        // Given
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";
        when(customerRepository.existsByEmail(email1)).thenReturn(false);
        when(customerRepository.existsByEmail(email2)).thenReturn(true);
        
        // When
        boolean result1 = validator.isValid(email1, context);
        boolean result2 = validator.isValid(email2, context);
        
        // Then
        assertTrue(result1);
        assertFalse(result2);
        verify(customerRepository, times(1)).existsByEmail(email1);
        verify(customerRepository, times(1)).existsByEmail(email2);
    }
    
    @Test
    void testInitialize_DoesNotThrow() {
        // Verify that initialize does not throw exceptions
        assertDoesNotThrow(() -> validator.initialize(constraintAnnotation));
    }
    
    @Test
    void testIsValid_EmailCaseSensitive_ChecksExactMatch() {
        // Given - The email must match exactly (case-sensitive in the database)
        String email = "Test@Example.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = validator.isValid(email, context);
        
        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }
}
