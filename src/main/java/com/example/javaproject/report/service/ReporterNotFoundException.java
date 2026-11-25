package com.example.javaproject.report.service;

/**
 * Indicates that the authenticated account no longer exists in storage.
 */
public class ReporterNotFoundException extends RuntimeException {

    /**
     * Creates the missing-reporter exception.
     */
    public ReporterNotFoundException() {
        super("Authenticated account no longer exists");
    }
}
