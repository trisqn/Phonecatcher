package com.example.javaproject.auth.service;

/**
 * Indicates that a password violates the authentication password constraints.
 */
public class InvalidPasswordException extends RuntimeException {

    /**
     * Creates the invalid-password exception.
     */
    public InvalidPasswordException() {
        super("Password must not exceed 72 UTF-8 bytes");
    }
}
