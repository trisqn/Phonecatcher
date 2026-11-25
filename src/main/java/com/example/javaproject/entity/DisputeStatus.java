package com.example.javaproject.entity;

/**
 * Lifecycle states for a submitted warning dispute.
 */
public enum DisputeStatus {
    /** Awaiting moderator review. */
    SUBMITTED,
    /** Accepted by a moderator. */
    ACCEPTED,
    /** Rejected by a moderator. */
    REJECTED
}
