package com.tmforum.openapi.config;

/**
 * Security constants for scopes and roles
 */
public class SecurityConstants {
    
    // Scopes for CRUD operations
    public static final String SCOPE_CUSTOMERS_READ = "SCOPE_customers:read";
    public static final String SCOPE_CUSTOMERS_WRITE = "SCOPE_customers:write";
    public static final String SCOPE_CUSTOMERS_DELETE = "SCOPE_customers:delete";
    public static final String SCOPE_CUSTOMERS_ADMIN = "SCOPE_customers:admin";
    
    // Roles (optional, if you also use roles in addition to scopes)
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    private SecurityConstants() {
        // Utility class, not instantiable
    }
}

