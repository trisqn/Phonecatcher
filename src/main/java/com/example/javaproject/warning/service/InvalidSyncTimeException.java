package com.example.javaproject.warning.service;

/**
 * Indicates that a warning-sync timestamp is later than the server's current time.
 */
public class InvalidSyncTimeException extends RuntimeException {

    /**
     * Creates the invalid-sync-time exception.
     */
    public InvalidSyncTimeException() {
        super("updatedAfter must not be later than the current server time");
    }
}
