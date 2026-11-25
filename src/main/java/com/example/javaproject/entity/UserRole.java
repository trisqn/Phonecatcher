package com.example.javaproject.entity;

/**
 * Authorization roles supported by the application.
 */
public enum UserRole {
    /** Regular account that can submit reports and disputes. */
    USER,
    /** Account permitted to review disputes. */
    MODERATOR,
    /** Account permitted to administer categories and review disputes. */
    ADMIN
}
