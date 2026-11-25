package com.example.javaproject.dispute.controller;

import com.example.javaproject.auth.controller.AuthExceptionHandler;
import com.example.javaproject.dispute.dto.CreateDisputeRequest;
import com.example.javaproject.dispute.dto.DisputeResponse;
import com.example.javaproject.dispute.dto.ReviewDisputeRequest;
import com.example.javaproject.dispute.service.DisputeService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes dispute submission and moderation endpoints.
 */
@RestController
@Tag(name = "Disputes", description = "Submit disputes about active warnings and review them as a moderator or administrator.")
public class DisputeController {

    private final DisputeService disputeService;

    /**
     * Creates the controller with its dispute service.
     *
     * @param disputeService dispute submission and moderation operations
     */
    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    /**
     * Submits a dispute against an active warning.
     *
     * @param warningId warning being disputed
     * @param request dispute explanation
     * @param authentication authenticated submitter
     * @return the submitted dispute
     */
    @PostMapping("/api/v1/warnings/{warningId}/disputes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Submit a dispute",
            description = "Authenticated users can dispute an active warning. A user may have only one submitted (open) dispute for a warning; submitting a dispute does not itself change warning status.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Dispute submitted"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
                    @ApiResponse(responseCode = "404", description = "Warning does not exist",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "409", description = "Warning is not active or an open dispute already exists",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public DisputeResponse submitDispute(
            @Parameter(description = "Identifier of the active warning being disputed", example = "1")
            @PathVariable Long warningId,
            @Valid @RequestBody CreateDisputeRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return disputeService.submitDispute(warningId, authentication.getName(), request.reason());
    }

    /**
     * Lists disputes awaiting moderator or administrator review.
     *
     * @return submitted disputes ordered by submission time
     */
    @GetMapping("/api/v1/moderation/disputes")
    @Operation(
            summary = "List submitted disputes",
            description = "Returns disputes awaiting a decision. Requires MODERATOR or ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submitted disputes"),
                    @ApiResponse(responseCode = "403", description = "MODERATOR or ADMIN role required")
            })
    public List<DisputeResponse> getSubmittedDisputes() {
        return disputeService.getSubmittedDisputes();
    }

    /**
     * Records a moderator decision and withdraws the warning when accepted.
     *
     * @param disputeId dispute being reviewed
     * @param request decision and required review comment
     * @param authentication authenticated reviewer
     * @return the reviewed dispute
     */
    @PutMapping("/api/v1/moderation/disputes/{disputeId}")
    @Operation(
            summary = "Review a dispute",
            description = "Requires MODERATOR or ADMIN role. The decision must be ACCEPTED or REJECTED and include a review comment. Accepting withdraws the warning; rejecting leaves it active.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Dispute reviewed"),
                    @ApiResponse(responseCode = "400", description = "Invalid decision or missing review comment",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "403", description = "MODERATOR or ADMIN role required"),
                    @ApiResponse(responseCode = "404", description = "Dispute does not exist",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "409", description = "Dispute has already been reviewed",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public DisputeResponse reviewDispute(
            @Parameter(description = "Identifier of the submitted dispute", example = "1")
            @PathVariable Long disputeId,
            @Valid @RequestBody ReviewDisputeRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return disputeService.reviewDispute(disputeId, authentication.getName(), request);
    }
}
