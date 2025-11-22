package com.eddy.dream.exception;


public class ErrorCode {
    
    // Business Errors (4xx)
    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String BUSINESS_ERROR_INTERNAL = "BUS001";
    
    // Resource Errors (4xx)
    public static final String RESOURCE_NOT_FOUND = "NOT_FOUND";
    public static final String RESOURCE_NOT_FOUND_INTERNAL = "RES001";
    
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String DUPLICATE_RESOURCE_INTERNAL = "RES002";
    
    // Authentication & Authorization Errors (401, 403)
    public static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    public static final String AUTHENTICATION_ERROR_INTERNAL = "AUTH001";
    
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_CREDENTIALS_INTERNAL = "AUTH002";
    
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String ACCESS_DENIED_INTERNAL = "AUTH003";
    
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String TOKEN_EXPIRED_INTERNAL = "AUTH004";
    
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String INVALID_TOKEN_INTERNAL = "AUTH005";
    
    // Validation Errors (400)
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String VALIDATION_ERROR_INTERNAL = "VAL001";
    
    // Server Errors (5xx)
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_ERROR";
    public static final String INTERNAL_SERVER_ERROR_INTERNAL = "SRV001";
    
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String DATABASE_ERROR_INTERNAL = "SRV002";
    
    public static final String EXTERNAL_SERVICE_ERROR = "SERVICE_UNAVAILABLE";
    public static final String EXTERNAL_SERVICE_ERROR_INTERNAL = "SRV003";
    
    // User-specific errors
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_NOT_FOUND_INTERNAL = "USR001";
    
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String USER_ALREADY_EXISTS_INTERNAL = "USR002";
    
    public static final String USER_INACTIVE = "USER_INACTIVE";
    public static final String USER_INACTIVE_INTERNAL = "USR003";
    
    public static final String USER_LOCKED = "USER_LOCKED";
    public static final String USER_LOCKED_INTERNAL = "USR004";
    
    private ErrorCode() {
        // Prevent instantiation
    }
}

