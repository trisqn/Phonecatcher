package com.example.javaproject.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Input describing why a user disputes an active warning.
 *
 * @param reason plain-text explanation for the dispute
 */
@Schema(description = "Reason for disputing an active warning.")
public record CreateDisputeRequest(
        @Schema(description = "Plain-text explanation of why the warning is disputed.", example = "This number has been reassigned and is not associated with the reported activity.")
        @NotBlank @Size(max = 4000) String reason
) {
}
