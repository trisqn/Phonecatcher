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
     * Creates an empty dispute for JPA entity materialization.
     */
    protected Dispute() {
    }

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
     * Gets the dispute identifier.
     *
     * @return dispute identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the warning being disputed.
     *
     * @return warning being disputed
     */
    public Warning getWarning() {
        return warning;
    }

    /**
     * Gets the account that submitted the dispute.
     *
     * @return account that submitted the dispute
     */
    public User getSubmitter() {
        return submitter;
    }

    /**
     * Gets the moderator who reviewed the dispute.
     *
     * @return moderator who reviewed the dispute, if any
     */
    public User getReviewer() {
        return reviewer;
    }

    /**
     * Gets the submitter's explanation.
     *
     * @return submitter's explanation
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the dispute's current lifecycle state.
     *
     * @return current lifecycle state
     */
    public DisputeStatus getStatus() {
        return status;
    }

    /**
     * Gets the moderator's explanation.
     *
     * @return moderator's explanation, if reviewed
     */
    public String getReviewComment() {
        return reviewComment;
    }

    /**
     * Gets the dispute submission time.
     *
     * @return dispute submission time
     */
    public Instant getSubmittedAt() {
        return submittedAt;
    }

    /**
     * Gets the time the dispute was reviewed.
     *
     * @return moderation time, or {@code null} if not reviewed
     */
    public Instant getReviewedAt() {
        return reviewedAt;
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
