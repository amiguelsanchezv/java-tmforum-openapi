package com.tmforum.openapi.controller;

import com.tmforum.openapi.dto.LoginRequest;
import com.tmforum.openapi.dto.LoginResponse;
import com.tmforum.openapi.service.AuthService;
import com.tmforum.openapi.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication and JWT token generation")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Operation(summary = "Login", description = "Generates a JWT token for authentication using clientId and clientSecret. " +
            "Scopes are obtained from the application configuration in the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token generated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Operation(summary = "Validate token", description = "Validates if a JWT token is valid")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);
            return ResponseEntity.ok().body("{\"valid\": " + isValid + "}");
        }
        return ResponseEntity.badRequest().body("{\"error\": \"Token not provided\"}");
    }
}

