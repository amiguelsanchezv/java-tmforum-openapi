package com.tmforum.openapi.service;

import com.tmforum.openapi.dto.LoginRequest;
import com.tmforum.openapi.dto.LoginResponse;
import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.repository.ApplicationRepository;
import com.tmforum.openapi.util.JwtUtil;
import com.tmforum.openapi.config.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    
    private Application application;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        application = new Application();
        application.setId("1");
        application.setClientId("app-test");
        application.setClientSecret("$2a$10$hashedSecret");
        application.setName("Test Application");
        application.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        application.setStatus(true);
        
        loginRequest = new LoginRequest();
        loginRequest.setClientId("app-test");
        loginRequest.setClientSecret("secret-test");
    }
    
    @Test
    void testLogin_Successful() {
        // Given
        when(applicationRepository.findByClientIdAndStatusTrue("app-test"))
            .thenReturn(Optional.of(application));
        when(passwordEncoder.matches("secret-test", "$2a$10$hashedSecret")).thenReturn(true);
        when(jwtUtil.generateToken("app-test", SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE))
            .thenReturn("token-jwt-test");
        
        // When
        LoginResponse response = authService.login(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("token-jwt-test", response.getToken());
        assertEquals("app-test", response.getClientId());
        assertNotNull(response.getScopes());
        assertEquals(2, response.getScopes().length);
        verify(applicationRepository, times(1)).findByClientIdAndStatusTrue("app-test");
        verify(passwordEncoder, times(1)).matches("secret-test", "$2a$10$hashedSecret");
        verify(jwtUtil, times(1)).generateToken(anyString(), any(String[].class));
    }
    
    @Test
    void testLogin_ClientIdNotFound() {
        // Given
        when(applicationRepository.findByClientIdAndStatusTrue("app-nonexistent"))
            .thenReturn(Optional.empty());
        
        loginRequest.setClientId("app-nonexistent");
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("ClientId not found or inactive", exception.getMessage());
        verify(applicationRepository, times(1)).findByClientIdAndStatusTrue("app-nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any(String[].class));
    }
    
    @Test
    void testLogin_ClientSecretIncorrect() {
        // Given
        when(applicationRepository.findByClientIdAndStatusTrue("app-test"))
            .thenReturn(Optional.of(application));
        when(passwordEncoder.matches("secret-incorrect", "$2a$10$hashedSecret")).thenReturn(false);
        
        loginRequest.setClientSecret("secret-incorrect");
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Incorrect ClientSecret", exception.getMessage());
        verify(applicationRepository, times(1)).findByClientIdAndStatusTrue("app-test");
        verify(passwordEncoder, times(1)).matches("secret-incorrect", "$2a$10$hashedSecret");
        verify(jwtUtil, never()).generateToken(anyString(), any(String[].class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplication_Successful() {
        // Given
        when(applicationRepository.existsByClientId("app-new")).thenReturn(false);
        when(passwordEncoder.encode("secret-new")).thenReturn("$2a$10$hashed");
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0, Application.class);
            app.setId("2");
            return app;
        });
        
        List<String> scopes = Arrays.asList("customers:read");
        
        // When
        Application result = authService.createApplication(
            "app-new", "secret-new", "New App", "Description", scopes);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("app-new", result.getClientId());
        assertTrue(result.getStatus());
        verify(applicationRepository, times(1)).existsByClientId("app-new");
        verify(passwordEncoder, times(1)).encode("secret-new");
        verify(applicationRepository, times(1)).save(any(Application.class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplication_DuplicateClientId() {
        // Given
        when(applicationRepository.existsByClientId("app-existing")).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.createApplication("app-existing", "secret", "App", "Desc", Arrays.asList("read"));
        });
        
        assertEquals("An application with that clientId already exists", exception.getMessage());
        verify(applicationRepository, times(1)).existsByClientId("app-existing");
        verify(applicationRepository, never()).save(any(Application.class));
    }
}

