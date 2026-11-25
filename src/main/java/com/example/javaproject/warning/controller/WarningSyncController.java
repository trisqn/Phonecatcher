package com.example.javaproject.warning.controller;

import com.example.javaproject.warning.dto.WarningSyncResponse;
import com.example.javaproject.warning.service.WarningQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Exposes initial and incremental warning synchronization.
 */
@RestController
@RequestMapping("/api/v1/sync/warnings")
@Tag(name = "Warning synchronization", description = "Initial and incremental warning synchronization for clients.")
public class WarningSyncController {

    private final WarningQueryService warningQueryService;

    /**
     * Creates the controller with its warning query service.
     *
     * @param warningQueryService warning synchronization operations
     */
    public WarningSyncController(WarningQueryService warningQueryService) {
        this.warningQueryService = warningQueryService;
    }

    /**
     * Returns an initial active-warning snapshot or warning changes after a sync cursor.
     *
     * @param updatedAfter previous response's server-generated sync time, or {@code null} for initial sync
     * @return changed warnings and a server-generated cursor for the next request
     */
    @GetMapping
    @Operation(
            summary = "Synchronize warnings",
            description = """
                    Public endpoint. Omit updatedAfter for the initial sync, which returns active warnings.
                    For subsequent syncs pass the syncTime from the previous successful response. Incremental
                    responses include warning states changed after the cursor, including inactive states, so
                    clients can update or remove their local active-warning view. Use the server-provided
                    syncTime rather than the client clock.
                    """
    )
    public WarningSyncResponse syncWarnings(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "UTC ISO-8601 timestamp from the previous response's syncTime; omit for the initial sync.",
                    example = "2026-11-25T08:30:00Z")
            Instant updatedAfter
    ) {
        return warningQueryService.getWarningsForSync(updatedAfter);
    }
}
