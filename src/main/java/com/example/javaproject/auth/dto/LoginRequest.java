package com.example.javaproject.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Credentials submitted when requesting an access token.
 *
 * @param email registered account email address
 * @param password account password
 */
@Schema(description = "Email and password used to authenticate.")
public record LoginRequest(
        @Schema(description = "Registered account email address.", example = "user@example.com", maxLength = 320)
        @NotBlank @Email @Size(max = 320) String email,
        @Schema(description = "Account password.", example = "CorrectHorseBattery1")
        @NotBlank @Size(min = 1, max = 72) String password
) {
}
