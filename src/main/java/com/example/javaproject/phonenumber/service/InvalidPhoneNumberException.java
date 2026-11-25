package com.example.javaproject.phonenumber.service;

/**
 * Indicates that a supplied phone number is not a valid international number.
 */
public class InvalidPhoneNumberException extends RuntimeException {

    /**
     * Creates the invalid-phone-number exception.
     */
    public InvalidPhoneNumberException() {
        super("Phone number must be valid and include its international country calling code");
    }
}
