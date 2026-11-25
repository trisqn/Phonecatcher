package com.example.javaproject.report.service;

/**
 * Indicates that a report's incident date is later than the current date.
 */
public class IncidentDateInFutureException extends RuntimeException {

    /**
     * Creates the future-incident-date exception.
     */
    public IncidentDateInFutureException() {
        super("Incident date must not be in the future");
    }
}
