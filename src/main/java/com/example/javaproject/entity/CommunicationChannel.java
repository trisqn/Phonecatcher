package com.example.javaproject.entity;

/**
 * Communication method used to contact a person in a reported incident.
 */
public enum CommunicationChannel {
    /** A voice call. */
    PHONE_CALL,
    /** A text message. */
    SMS,
    /** A third-party messaging application. */
    MESSAGING_APP,
    /** An email message. */
    EMAIL,
    /** Another communication method. */
    OTHER
}
