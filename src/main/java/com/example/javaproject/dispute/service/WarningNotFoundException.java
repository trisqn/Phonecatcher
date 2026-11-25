package com.example.javaproject.dispute.service;

/**
 * Indicates that a requested warning does not exist.
 */
public class WarningNotFoundException extends RuntimeException {

    /**
     * Creates the missing-warning exception.
     *
     * @param id identifier of the warning that was not found
     */
    public WarningNotFoundException(Long id) {
        super("Warning not found: " + id);
    }
}
