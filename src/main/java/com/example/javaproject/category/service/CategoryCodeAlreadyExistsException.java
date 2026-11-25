package com.example.javaproject.category.service;

/**
 * Indicates that a category code is already assigned.
 */
public class CategoryCodeAlreadyExistsException extends RuntimeException {

    /**
     * Creates the duplicate-code exception.
     *
     * @param code category code that is already in use
     */
    public CategoryCodeAlreadyExistsException(String code) {
        super("A category with code '" + code + "' already exists");
    }
}
