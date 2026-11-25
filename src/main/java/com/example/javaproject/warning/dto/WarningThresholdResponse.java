package com.example.javaproject.warning.dto;

import java.time.Instant;

/**
 * Configured report threshold and its last update time.
 *
 * @param reportThreshold minimum distinct reporter count
 * @param updatedAt instant the configuration was last changed
 */
public record WarningThresholdResponse(int reportThreshold, Instant updatedAt) {
}
