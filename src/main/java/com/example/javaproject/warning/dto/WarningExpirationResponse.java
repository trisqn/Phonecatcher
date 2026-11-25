package com.example.javaproject.warning.dto;

import java.time.Instant;

/**
 * Configured warning inactivity period and its last update time.
 *
 * @param expirationDays number of inactive days before expiration
 * @param updatedAt instant the configuration was last changed
 */
public record WarningExpirationResponse(int expirationDays, Instant updatedAt) {
}
