package com.example.javaproject.dispute.service;

/**
 * Indicates that a dispute can no longer be reviewed because it is final.
 */
public class DisputeAlreadyReviewedException extends RuntimeException {

    /**
     * Creates the already-reviewed exception.
     */
    public DisputeAlreadyReviewedException() {
        super("Only submitted disputes can be reviewed");
    }
}
