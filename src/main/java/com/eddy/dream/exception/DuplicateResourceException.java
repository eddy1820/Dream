package com.eddy.dream.exception;

public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
    
    public DuplicateResourceException(String resourceName, String field, String value) {
        super("DUPLICATE_RESOURCE", 
            String.format("%s already exists, %s: %s", resourceName, field, value));
    }
}

