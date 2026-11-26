package com.example.javaproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A user's challenge to an active warning, including its moderation outcome.
 */
@Entity
@Table(
        name = "dispute",
        indexes = {
                @Index(name = "ix_dispute_warning_id", columnList = "warning_id"),
                @Index(name = "ix_dispute_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warning_id", nullable = false)
    private Warning warning;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status = DisputeStatus.SUBMITTED;

    @Column(name = "review_comment", columnDefinition = "text")
    private String reviewComment;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /**
     * Creates a dispute awaiting moderator review.
     *
     * @param warning active warning being disputed
     * @param submitter account submitting the dispute
     * @param reason explanation provided by the submitter
     */
    public Dispute(Warning warning, User submitter, String reason) {
        this.warning = warning;
        this.submitter = submitter;
        this.reason = reason;
    }

    @PrePersist
    void setSubmittedAt() {
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }

    /**
     * Records a final moderator decision and its audit details.
     *
     * @param reviewer user making the decision
     * @param decision accepted or rejected outcome
     * @param reviewComment required moderator explanation
     * @param reviewedAt decision timestamp
     * @throws IllegalStateException if this dispute has already been reviewed
     */
    public void review(User reviewer, DisputeStatus decision, String reviewComment, Instant reviewedAt) {
        if (status != DisputeStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted disputes can be reviewed");
        }
        if (decision != DisputeStatus.ACCEPTED && decision != DisputeStatus.REJECTED) {
            throw new IllegalArgumentException("Review decision must be ACCEPTED or REJECTED");
        }
        this.reviewer = reviewer;
        this.status = decision;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
    }
}
