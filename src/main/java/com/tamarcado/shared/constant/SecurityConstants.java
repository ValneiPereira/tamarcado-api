package com.tamarcado.shared.constant;

public final class SecurityConstants {
    
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "roles";
    public static final String USER_ID_KEY = "userId";
    
    /**
     * Endpoints públicos.
     * Paths relativos ao context-path (/api/v1): Spring Security faz match
     * no servletPath, que não inclui o context-path.
     */
    public static final String[] PUBLIC_ENDPOINTS = {
        "/auth/**",
        "/geocoding/**",
        "/reviews/professionals/**",
        "/search/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api-docs/**",
        "/v3/api-docs/**",
        "/actuator/health",
        "/actuator/info"
    };
    
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}