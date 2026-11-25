package com.example.javaproject.warning.dto;

import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Requested minimum number of distinct reports for warning generation.
 *
 * @param reportThreshold required distinct reporter count
 */
@Schema(description = "Minimum number of distinct reporting users required for a warning.")
public record WarningThresholdRequest(
        @Schema(description = "Must be at least 2. Updating recalculates warnings immediately.", example = "3", minimum = "2")
        @Min(2) int reportThreshold
) {
}
