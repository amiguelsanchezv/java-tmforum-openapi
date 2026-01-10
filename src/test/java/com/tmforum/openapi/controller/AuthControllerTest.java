package com.tmforum.openapi.controller;

import com.tmforum.openapi.dto.LoginRequest;
import com.tmforum.openapi.dto.LoginResponse;
import com.tmforum.openapi.service.AuthService;
import com.tmforum.openapi.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    
    @Mock
    private AuthService authService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthController authController;
    
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    
    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setClientId("app-admin");
        loginRequest.setClientSecret("secret-admin");
        
        loginResponse = new LoginResponse();
        loginResponse.setClientId("app-admin");
        loginResponse.setToken("test-token");
    }
    
    @Test
    void testLogin_Success() {
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);
        
        ResponseEntity<?> response = authController.login(loginRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService, times(1)).login(loginRequest);
    }
    
    @Test
    void testLogin_InvalidCredentials() {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));
        
        ResponseEntity<?> response = authController.login(loginRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void testValidateToken_Valid() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        
        ResponseEntity<?> response = authController.validateToken("Bearer valid-token");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil, times(1)).validateToken("valid-token");
    }
    
    @Test
    void testValidateToken_Invalid() {
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);
        
        ResponseEntity<?> response = authController.validateToken("Bearer invalid-token");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil, times(1)).validateToken("invalid-token");
    }
    
    @Test
    void testValidateToken_NoBearer() {
        ResponseEntity<?> response = authController.validateToken("invalid-header");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(jwtUtil, never()).validateToken(any());
    }
    
    @Test
    void testValidateToken_NullHeader() {
        ResponseEntity<?> response = authController.validateToken(null);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(jwtUtil, never()).validateToken(any());
    }
    
    @Test
    void testValidateToken_EmptyBearer() {
        ResponseEntity<?> response = authController.validateToken("Bearer ");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil, times(1)).validateToken("");
    }
}

