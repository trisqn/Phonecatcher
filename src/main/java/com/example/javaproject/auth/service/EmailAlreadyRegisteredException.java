package com.example.javaproject.auth.service;

/**
 * Indicates that registration attempted to reuse an existing email address.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    /**
     * Creates the duplicate-email exception.
     */
    public EmailAlreadyRegisteredException() {
        super("An account with this email already exists");
    }
}
