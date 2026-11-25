package com.example.javaproject.dispute.service;

/**
 * Indicates that a dispute cannot be submitted because the warning is inactive.
 */
public class WarningNotActiveException extends RuntimeException {

    /**
     * Creates the inactive-warning exception.
     */
    public WarningNotActiveException() {
        super("Only active warnings can be disputed");
    }
}
