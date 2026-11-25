package com.example.javaproject.dispute.service;

/**
 * Indicates that the user already has an open dispute for the warning.
 */
public class DuplicateOpenDisputeException extends RuntimeException {

    /**
     * Creates the duplicate-open-dispute exception.
     */
    public DuplicateOpenDisputeException() {
        super("You already have a submitted dispute for this warning");
    }
}
