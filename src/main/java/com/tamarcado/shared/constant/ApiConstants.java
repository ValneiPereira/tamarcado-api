package com.tamarcado.shared.constant;

public final class ApiConstants {
    
    public static final String API_BASE_PATH = "/api/v1";
    
    public static final String AUTH_PATH = API_BASE_PATH + "/auth";
    public static final String USERS_PATH = API_BASE_PATH + "/users";
    public static final String PROFESSIONALS_PATH = API_BASE_PATH + "/professionals";
    public static final String APPOINTMENTS_PATH = API_BASE_PATH + "/appointments";
    public static final String REVIEWS_PATH = API_BASE_PATH + "/reviews";
    public static final String SEARCH_PATH = API_BASE_PATH + "/search";
    public static final String NOTIFICATIONS_PATH = API_BASE_PATH + "/notifications";
    public static final String GEOCODING_PATH = API_BASE_PATH + "/geocoding";
    public static final String DASHBOARD_PATH = API_BASE_PATH + "/dashboard";
    
    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}