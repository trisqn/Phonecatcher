package com.example.javaproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * An account that can submit reports and hold an application role.
 */
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Creates an empty account for JPA entity materialization.
     */
    protected User() {
    }

    /**
     * Creates an account with the supplied credentials and role.
     *
     * @param email normalized account email address
     * @param passwordHash encoded password, never the plain-text password
     * @param role assigned application role
     */
    public User(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Gets the account identifier.
     *
     * @return account identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the account email address.
     *
     * @return account email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the encoded password hash.
     *
     * @return encoded password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Gets the account's assigned role.
     *
     * @return account role
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Checks whether the account is enabled.
     *
     * @return whether the account is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the account creation time.
     *
     * @return account creation time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
