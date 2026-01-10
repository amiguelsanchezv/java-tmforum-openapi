package com.tmforum.openapi.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("message", "Validation error");
        errors.put("errors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotWritableException(
            HttpMessageNotWritableException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Error serializing response");
        
        Throwable cause = ex.getCause();
        if (cause instanceof JsonMappingException) {
            error.put("detail", "JSON mapping error: " + cause.getMessage());
            error.put("type", "JsonMappingException");
        } else if (cause instanceof JsonProcessingException) {
            error.put("detail", "JSON processing error: " + cause.getMessage());
            error.put("type", "JsonProcessingException");
        } else {
            error.put("detail", ex.getMessage());
            if (cause != null) {
                error.put("cause", cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
        }
        
        // Full log for debugging
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Internal server error");
        error.put("detail", ex.getMessage());
        error.put("type", ex.getClass().getSimpleName());
        
        // Full log for debugging
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

