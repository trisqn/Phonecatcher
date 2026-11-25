package com.example.javaproject.report.controller;

import com.example.javaproject.auth.controller.AuthExceptionHandler;
import com.example.javaproject.report.dto.CreateReportRequest;
import com.example.javaproject.report.dto.ReportResponse;
import com.example.javaproject.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes authenticated report-submission endpoints.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Submit authenticated reports about suspicious phone-number activity.")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    /**
     * Creates the controller with its report service.
     *
     * @param reportService report creation operations
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Submits a report for the authenticated account.
     *
     * @param request incident details
     * @param authentication authenticated reporter
     * @return the created report
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Submit a report",
            description = "Creates a report for the authenticated user and recalculates the phone number's warning in the same transaction. The category must currently be active; incidentDate cannot be in the future.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Report created"),
                    @ApiResponse(responseCode = "400", description = "Invalid phone number, future incident date, or request validation failure",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
                    @ApiResponse(responseCode = "404", description = "Category is unavailable",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public ReportResponse createReport(
            @Valid @RequestBody CreateReportRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return reportService.createReport(authentication.getName(), request);
    }
}
