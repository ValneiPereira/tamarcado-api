package com.tamarcado.shared.constant;

public final class SecurityConstants {
    
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "roles";
    public static final String USER_ID_KEY = "userId";
    
    // Endpoints públicos
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/**",
        "/api/v1/swagger-ui/**",
        "/api/v1/api-docs/**",
        "/actuator/health",
        "/actuator/info"
    };
    
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}