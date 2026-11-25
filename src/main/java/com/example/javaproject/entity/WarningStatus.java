package com.example.javaproject.entity;

/**
 * Lifecycle states for a generated warning.
 */
public enum WarningStatus {
    /** Warning currently meets the reporting threshold. */
    ACTIVE,
    /** Warning expired after the configured inactivity period. */
    EXPIRED,
    /** Warning was withdrawn after an accepted dispute. */
    WITHDRAWN,
    /** Warning no longer meets the reporting threshold. */
    THRESHOLD_NOT_MET
}
