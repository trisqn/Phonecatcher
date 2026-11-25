package com.example.javaproject.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Short-lived access token returned after successful authentication.
 *
 * @param accessToken signed JWT value
 * @param tokenType authorization scheme
 * @param expiresAt token expiration instant
 */
@Schema(description = "Short-lived JWT access token response.")
public record TokenResponse(
        @Schema(description = "JWT to send in the Authorization header as a Bearer token.")
        String accessToken,
        @Schema(description = "Authorization scheme.", example = "Bearer")
        String tokenType,
        @Schema(description = "Token expiration instant in UTC.")
        Instant expiresAt
) {
}
