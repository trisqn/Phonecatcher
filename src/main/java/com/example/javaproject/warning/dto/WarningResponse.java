package com.example.javaproject.warning.dto;

import com.example.javaproject.entity.WarningStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Public summary of a warning, excluding reporter identities and report descriptions.
 *
 * @param id warning identifier
 * @param phoneNumber normalized phone number in E.164 format
 * @param categoryCode stable code of the dominant category
 * @param categoryName display name of the dominant category
 * @param title user-facing warning title
 * @param status current warning state
 * @param activatedAt instant the warning became active
 * @param updatedAt last update instant used by incremental synchronization
 */
@Schema(description = "Public warning summary. Does not expose reporter identities or report descriptions.")
public record WarningResponse(
        @Schema(description = "Warning identifier.") Long id,
        @Schema(description = "Normalized international phone number (E.164).", example = "+37255512345") String phoneNumber,
        @Schema(description = "Code of the dominant impersonation category.", example = "POLICE") String categoryCode,
        @Schema(description = "Display name of the dominant category.") String categoryName,
        @Schema(description = "User-facing warning title.") String title,
        @Schema(description = "Current warning state.") WarningStatus status,
        @Schema(description = "When this warning became active.") Instant activatedAt,
        @Schema(description = "When this warning was last changed; used by incremental sync.") Instant updatedAt
) {
}
