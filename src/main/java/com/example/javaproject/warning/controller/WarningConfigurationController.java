package com.example.javaproject.warning.controller;

import com.example.javaproject.auth.controller.AuthExceptionHandler;
import com.example.javaproject.warning.dto.WarningThresholdRequest;
import com.example.javaproject.warning.dto.WarningThresholdResponse;
import com.example.javaproject.warning.dto.WarningExpirationRequest;
import com.example.javaproject.warning.dto.WarningExpirationResponse;
import com.example.javaproject.warning.service.WarningConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes administrator endpoints for warning-generation settings.
 */
@RestController
@RequestMapping("/api/v1/admin/warning-threshold")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Warning configuration", description = "Change the reporting threshold and inactivity expiration. Requires the ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class WarningConfigurationController {

    private final WarningConfigurationService configurationService;

    /**
     * Creates the controller with its configuration service.
     *
     * @param configurationService warning configuration operations
     */
    public WarningConfigurationController(WarningConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Returns the configured distinct-reporter threshold.
     *
     * @return the current threshold and last update time
     */
    @GetMapping
    @Operation(summary = "Get the reporting threshold", description = "Returns the minimum number of distinct reporting users required to create a warning.")
    public WarningThresholdResponse getThreshold() {
        return configurationService.getThreshold();
    }

    /**
     * Updates the report threshold and recalculates phone-number warnings.
     *
     * @param request new threshold, at least two
     * @return the saved threshold and update time
     */
    @PutMapping
    @Operation(
            summary = "Update the reporting threshold",
            description = "Sets a threshold of at least two distinct users and immediately recalculates all known phone numbers. Active warnings that no longer qualify are deactivated.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Threshold updated and warnings recalculated"),
                    @ApiResponse(responseCode = "400", description = "Threshold must be at least 2",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "403", description = "ADMIN role required")
            })
    public WarningThresholdResponse updateThreshold(@Valid @RequestBody WarningThresholdRequest request) {
        return configurationService.updateThreshold(request.reportThreshold());
    }

    /**
     * Returns the inactivity period used to expire active warnings.
     *
     * @return configured expiration duration in days and update time
     */
    @GetMapping("/expiration")
    @Operation(summary = "Get the warning expiration period", description = "Returns the configured inactivity period in days. Default is 60 days.")
    public WarningExpirationResponse getExpirationPeriod() {
        return configurationService.getExpirationPeriod();
    }

    /**
     * Changes the inactivity period and immediately reevaluates active warnings.
     *
     * @param request new expiration period between 1 and 3650 days
     * @return saved expiration period and update time
     */
    @PutMapping("/expiration")
    @Operation(
            summary = "Update the warning expiration period",
            description = "Accepts 1–3650 days and immediately reevaluates active warnings. Shortening the period may expire warnings immediately. Extending it does not reactivate warnings that have already expired.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Expiration period updated and active warnings reevaluated"),
                    @ApiResponse(responseCode = "400", description = "Expiration period is outside the allowed range",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "403", description = "ADMIN role required")
            })
    public WarningExpirationResponse updateExpirationPeriod(
            @Valid @RequestBody WarningExpirationRequest request
    ) {
        return configurationService.updateExpirationPeriod(request.expirationDays());
    }
}
