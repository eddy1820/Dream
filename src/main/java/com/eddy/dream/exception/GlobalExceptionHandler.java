package com.eddy.dream.exception;

import com.eddy.dream.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle Business Exception
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, 
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Business exception: {}", traceId, ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .code(ErrorCode.BUSINESS_ERROR)
            .internalCode(ErrorCode.BUSINESS_ERROR_INTERNAL)
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle Resource Not Found Exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Resource not found: {}", traceId, ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .code(ErrorCode.RESOURCE_NOT_FOUND)
            .internalCode(ErrorCode.RESOURCE_NOT_FOUND_INTERNAL)
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle Duplicate Resource Exception
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Duplicate resource: {}", traceId, ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .code(ErrorCode.DUPLICATE_RESOURCE)
            .internalCode(ErrorCode.DUPLICATE_RESOURCE_INTERNAL)
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handle Authentication Exception
     */
    @ExceptionHandler({
        com.eddy.dream.exception.AuthenticationException.class,
        AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Authentication failed: {}", traceId, ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .code(ErrorCode.AUTHENTICATION_ERROR)
            .internalCode(ErrorCode.AUTHENTICATION_ERROR_INTERNAL)
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle Access Denied Exception
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Access denied: {}", traceId, ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .code(ErrorCode.ACCESS_DENIED)
            .internalCode(ErrorCode.ACCESS_DENIED_INTERNAL)
            .message("You do not have permission to access this resource")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle Validation Exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Validation failed: {}", traceId, ex.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                FieldError fieldError = (FieldError) error;
                return ErrorResponse.FieldError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .rejectedValue(fieldError.getRejectedValue())
                    .build();
            })
            .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .code(ErrorCode.VALIDATION_ERROR)
            .internalCode(ErrorCode.VALIDATION_ERROR_INTERNAL)
            .message("Validation failed")
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle Other Unexpected Exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Unexpected exception", traceId, ex);
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .code(ErrorCode.INTERNAL_SERVER_ERROR)
            .internalCode(ErrorCode.INTERNAL_SERVER_ERROR_INTERNAL)
            .message("Internal server error, please try again later")
            .details("Please contact support with trace ID: " + traceId)
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Generate a unique trace ID for tracking errors
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

