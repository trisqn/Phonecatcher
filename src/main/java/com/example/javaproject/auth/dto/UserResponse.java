package com.example.javaproject.auth.dto;

import com.example.javaproject.entity.UserRole;

import java.time.Instant;

/**
 * Public account details returned after registration.
 *
 * @param id account identifier
 * @param email account email address
 * @param role assigned account role
 * @param createdAt account creation time
 */
public record UserResponse(Long id, String email, UserRole role, Instant createdAt) {
}
