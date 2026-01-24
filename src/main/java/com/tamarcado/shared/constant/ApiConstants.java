package com.tamarcado.shared.constant;

/**
 * Paths relativos ao context-path (/api/v1).
 * O server.servlet.context-path define a base; os controllers mapeiam
 * paths relativos (ex.: /auth, /appointments).
 */
public final class ApiConstants {

    public static final String AUTH_PATH = "/auth";
    public static final String USERS_PATH = "/users";
    public static final String PROFESSIONALS_PATH = "/professionals";
    public static final String APPOINTMENTS_PATH = "/appointments";
    public static final String REVIEWS_PATH = "/reviews";
    public static final String SEARCH_PATH = "/search";
    public static final String NOTIFICATIONS_PATH = "/notifications";
    public static final String GEOCODING_PATH = "/geocoding";
    public static final String DASHBOARD_PATH = "/dashboard";

    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}