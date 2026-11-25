package com.example.javaproject.dispute.dto;

import com.example.javaproject.entity.DisputeStatus;

import java.time.Instant;

/**
 * Dispute details returned to submitters and moderators.
 *
 * @param id dispute identifier
 * @param warningId disputed warning identifier
 * @param phoneNumber normalized phone number associated with the warning
 * @param submitterId account that submitted the dispute
 * @param reason submitter's explanation
 * @param status current dispute state
 * @param reviewerId moderator account, or {@code null} while unreviewed
 * @param reviewComment moderator's explanation, if reviewed
 * @param submittedAt submission timestamp
 * @param reviewedAt review timestamp, or {@code null} while unreviewed
 */
public record DisputeResponse(
        Long id,
        Long warningId,
        String phoneNumber,
        Long submitterId,
        String reason,
        DisputeStatus status,
        Long reviewerId,
        String reviewComment,
        Instant submittedAt,
        Instant reviewedAt
) {
}
