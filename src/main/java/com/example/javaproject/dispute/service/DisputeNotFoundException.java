package com.example.javaproject.dispute.service;

/**
 * Indicates that a requested dispute does not exist.
 */
public class DisputeNotFoundException extends RuntimeException {

    /**
     * Creates the missing-dispute exception.
     *
     * @param id identifier of the dispute that was not found
     */
    public DisputeNotFoundException(Long id) {
        super("Dispute not found: " + id);
    }
}
