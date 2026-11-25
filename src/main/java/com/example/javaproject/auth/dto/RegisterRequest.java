package com.example.javaproject.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Credentials submitted to create a regular user account.
 *
 * @param email account email address
 * @param password account password
 */
@Schema(description = "Credentials for creating a regular user account.")
public record RegisterRequest(
        @Schema(description = "Account email address.", example = "user@example.com", maxLength = 320)
        @NotBlank @Email @Size(max = 320) String email,
        @Schema(description = "Password must contain 12–72 characters.", example = "CorrectHorseBattery1")
        @NotBlank @Size(min = 12, max = 72) String password
) {
}
