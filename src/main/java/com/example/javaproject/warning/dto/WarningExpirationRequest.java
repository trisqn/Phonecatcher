package com.example.javaproject.warning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Requested inactivity period for active warning expiration.
 *
 * @param expirationDays number of inactive days before expiration
 */
@Schema(description = "Inactivity period after which an active warning expires.")
public record WarningExpirationRequest(
        @Schema(description = "Allowed range is 1–3650 days. Updating immediately reevaluates active warnings.", example = "60", minimum = "1", maximum = "3650")
        @Min(1) @Max(3650) int expirationDays
) {
}
