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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Aggregated risk warning for a phone number based on submitted reports.
 */
@Entity
@Table(
        name = "warning",
        indexes = {
                @Index(name = "ix_warning_phone_number_id", columnList = "phone_number_id"),
                @Index(name = "ix_warning_status", columnList = "status"),
                @Index(name = "ix_warning_updated_at", columnList = "updated_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_number_id", nullable = false)
    private PhoneNumber phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dominant_category_id", nullable = false)
    private FraudCategory dominantCategory;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarningStatus status;

    @Column(name = "activated_at", nullable = false)
    private Instant activatedAt;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    /**
     * Creates an active warning for a phone number.
     *
     * @param phoneNumber number associated with this warning
     * @param dominantCategory category currently represented by the most reports
     * @param title warning title for the dominant category
     * @param activatedAt instant the warning became active
     */
    public Warning(
            PhoneNumber phoneNumber,
            FraudCategory dominantCategory,
            String title,
            Instant activatedAt
    ) {
        this.phoneNumber = phoneNumber;
        this.dominantCategory = dominantCategory;
        this.title = title;
        this.status = WarningStatus.ACTIVE;
        this.activatedAt = activatedAt;
    }

    @PrePersist
    void setCreationTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void setUpdatedAt() {
        updatedAt = Instant.now();
    }

    /**
     * Replaces the category and warning title with a newly dominant category.
     *
     * @param category category with a strictly higher report count
     */
    public void updateDominantCategory(FraudCategory category) {
        this.dominantCategory = category;
        this.title = category.getWarningTitle();
    }

    /**
     * Deactivates a warning whose current report count is below the configured threshold.
     *
     * @param deactivatedAt time of deactivation
     */
    public void markThresholdNotMet(Instant deactivatedAt) {
        this.status = WarningStatus.THRESHOLD_NOT_MET;
        this.deactivatedAt = deactivatedAt;
    }

    /**
     * Withdraws an active warning after an accepted dispute.
     *
     * @param deactivatedAt time of withdrawal
     * @throws IllegalStateException if this warning is not active
     */
    public void withdraw(Instant deactivatedAt) {
        if (status != WarningStatus.ACTIVE) {
            throw new IllegalStateException("Only active warnings can be withdrawn");
        }
        this.status = WarningStatus.WITHDRAWN;
        this.deactivatedAt = deactivatedAt;
    }

    /**
     * Expires an active warning after the configured inactivity period.
     *
     * @param deactivatedAt time of expiration
     * @throws IllegalStateException if this warning is not active
     */
    public void expire(Instant deactivatedAt) {
        if (status != WarningStatus.ACTIVE) {
            throw new IllegalStateException("Only active warnings can expire");
        }
        this.status = WarningStatus.EXPIRED;
        this.deactivatedAt = deactivatedAt;
    }
}
