package com.eddy.dream.exception;


public class InvalidCredentialsException extends AuthenticationException {
    
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

