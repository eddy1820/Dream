package com.eddy.dream.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Only include non-null fields in JSON
public class ErrorResponse {
    
    /**
     * HTTP status code (e.g., 400, 404, 500)
     */
    private int status;
    
    /**
     * Public error code - simple code for client consumption
     * Examples: "VALIDATION_ERROR", "NOT_FOUND", "UNAUTHORIZED"
     */
    private String code;
    
    /**
     * Internal error code - detailed code for logging and debugging
     * Examples: "USR001", "AUTH002", "DB003"
     */
    private String internalCode;
    
    /**
     * User-friendly error message
     */
    private String message;
    
    /**
     * Detailed error description
     */
    private String details;
    
    /**
     * Request path where the error occurred
     */
    private String path;
    
    /**
     * Timestamp when the error occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Trace ID for tracking the request across services (optional)
     */
    private String traceId;
    
    /**
     * List of validation errors (optional)
     */
    private List<FieldError> fieldErrors;
    
    /**
     * Field-specific validation error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
    
    /**
     * Create a simple error response
     */
    public static ErrorResponse of(int status, String message, String path) {
        return ErrorResponse.builder()
            .status(status)
            .message(message)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Create an error response with validation errors
     */
    public static ErrorResponse withFieldErrors(int status, String message, String path, List<FieldError> fieldErrors) {
        return ErrorResponse.builder()
            .status(status)
            .message(message)
            .path(path)
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

