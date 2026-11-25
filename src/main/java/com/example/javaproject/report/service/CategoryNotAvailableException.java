package com.example.javaproject.report.service;

/**
 * Indicates that a requested report category is missing or inactive.
 */
public class CategoryNotAvailableException extends RuntimeException {

    /**
     * Creates the unavailable-category exception.
     *
     * @param categoryCode requested category code
     */
    public CategoryNotAvailableException(String categoryCode) {
        super("Active category not found: " + categoryCode);
    }
}
