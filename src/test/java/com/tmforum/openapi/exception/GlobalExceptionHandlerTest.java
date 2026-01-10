package com.tmforum.openapi.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }
    
    @Test
    void testHandleValidationExceptions() {
        // Create a mock BindingResult
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("customerRequest", "email", "Email must have a valid format");
        
        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(fieldError));
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertEquals("Validation error", body.get("message"));
        assertTrue(body.containsKey("errors"));
    }
    
    @Test
    void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Customer not found");
        
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> body = Objects.requireNonNull(response.getBody());
        assertEquals("Customer not found", body.get("message"));
    }
    
    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("Internal server error");
        
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertEquals("Internal server error", body.get("message"));
        assertEquals("Internal server error", body.get("detail"));
        assertEquals("Exception", body.get("type"));
    }
    
    @Test
    void testHandleValidationExceptions_MultipleErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("customerRequest", "firstName", "The first name is required");
        FieldError error2 = new FieldError("customerRequest", "email", "The email must be a valid email address");
        
        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(error1, error2));
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("email"));
    }
    
    @Test
    void testHandleHttpMessageNotWritableException_WithJsonMappingException() {
        // Given
        JsonMappingException jsonMappingException = new JsonMappingException(null, "JSON mapping error");
        HttpMessageNotWritableException exception = new HttpMessageNotWritableException(
            "Error serializing", jsonMappingException);
        
        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotWritableException(exception);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertEquals("Error serializing response", body.get("message"));
        assertTrue(body.get("detail").toString().contains("JSON mapping error"));
        assertEquals("JsonMappingException", body.get("type"));
    }
    
    @Test
    void testHandleHttpMessageNotWritableException_WithJsonProcessingException() {
        // Given - JsonProcessingException is the base class, but not JsonMappingException
        JsonProcessingException jsonProcessingException = new JsonProcessingException("JSON processing error") {};
        HttpMessageNotWritableException exception = new HttpMessageNotWritableException(
            "Error serializing", jsonProcessingException);
        
        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotWritableException(exception);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
            assertEquals("Error serializing response", body.get("message"));
        assertTrue(body.get("detail").toString().contains("JSON processing error"));
        assertEquals("JsonProcessingException", body.get("type"));
    }
    
    @Test
    void testHandleHttpMessageNotWritableException_WithOtherCause() {
        // Given
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Unknown cause");
        HttpMessageNotWritableException exception = new HttpMessageNotWritableException(
            "Error serializing", illegalArgumentException);
        
        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotWritableException(exception);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertEquals("Error serializing response", body.get("message"));
        assertEquals("Error serializing", body.get("detail"));
        assertTrue(body.get("cause").toString().contains("IllegalArgumentException"));
        assertTrue(body.get("cause").toString().contains("Unknown cause"));
    }
    
    @Test
    void testHandleHttpMessageNotWritableException_WithNullCause() {
        // Given
        HttpMessageNotWritableException exception = new HttpMessageNotWritableException("Error serializing");
        
        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotWritableException(exception);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertEquals("Error serializing response", body.get("message"));
        assertEquals("Error serializing", body.get("detail"));
        assertFalse(body.containsKey("cause"));
    }
    
    @Test
    void testHandleRuntimeException_WithNullMessage() {
        // Given
        RuntimeException exception = new RuntimeException();
        
        // When
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(exception);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> body = Objects.requireNonNull(response.getBody());
        assertNull(body.get("message"));
    }
    
    @Test
    void testHandleGenericException_WithNullMessage() {
        // Given
        Exception exception = new Exception();
        
        // When
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(exception);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertEquals("Internal server error", body.get("message"));
        assertNull(body.get("detail"));
        assertEquals("Exception", body.get("type"));
    }
}

