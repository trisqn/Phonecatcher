package com.example.javaproject.category.service;

/**
 * Indicates that a requested category does not exist.
 */
public class CategoryNotFoundException extends RuntimeException {

    /**
     * Creates the missing-category exception.
     *
     * @param id identifier of the category that was not found
     */
    public CategoryNotFoundException(Long id) {
        super("Category " + id + " was not found");
    }
}
