package com.eddy.dream.enums;


public enum UserStatus {
    /**
     * Active - Can use the system normally
     */
    ACTIVE,
    
    /**
     * Inactive - Temporarily unable to use the system
     */
    INACTIVE,
    
    /**
     * Locked - Locked due to security reasons
     */
    LOCKED,
    
    /**
     * Pending Verification - Waiting for email verification
     */
    PENDING_VERIFICATION
}

