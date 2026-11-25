package com.example.javaproject.warning.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Warning changes and server-generated cursor returned by synchronization.
 *
 * @param items warning records active at the snapshot or changed since the cursor
 * @param syncTime server timestamp to pass as the next incremental-sync cursor
 */
@Schema(description = "Warnings returned by a sync request and the server timestamp to use as the next cursor.")
public record WarningSyncResponse(
        @Schema(description = "Warning records changed or active at the requested sync point.")
        List<WarningResponse> items,
        @Schema(description = "Server-generated UTC timestamp. Pass this value as updatedAfter on the next sync.")
        Instant syncTime
) {
}
