package com.example.javaproject.dispute.dto;

import com.example.javaproject.entity.DisputeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Moderator decision and the explanation recorded with it.
 *
 * @param decision final outcome, either {@code ACCEPTED} or {@code REJECTED}
 * @param reviewComment explanation for the moderation decision
 */
@Schema(description = "Moderator's decision and required review note.")
public record ReviewDisputeRequest(
        @Schema(description = "Accepting withdraws the warning; rejecting leaves it active.", allowableValues = {"ACCEPTED", "REJECTED"}, example = "REJECTED")
        @NotNull @Pattern(regexp = "ACCEPTED|REJECTED") String decision,
        @Schema(description = "Required explanation recorded with the decision.", example = "The available information does not support withdrawing this warning.")
        @NotBlank @Size(max = 4000) String reviewComment
) {
}
