package com.example.javaproject.dispute.service;

/**
 * Indicates that a requested dispute decision is not supported.
 */
public class InvalidDisputeDecisionException extends RuntimeException {

    /**
     * Creates the invalid-decision exception.
     */
    public InvalidDisputeDecisionException() {
        super("Decision must be ACCEPTED or REJECTED");
    }
}
