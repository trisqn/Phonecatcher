package com.example.javaproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Singleton database configuration for report thresholds and warning expiration.
 */
@Entity
@Table(name = "warning_configuration")
public class WarningConfiguration {

    /** Primary key used for the single configuration row. */
    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Column(name = "report_threshold", nullable = false)
    private int reportThreshold;

    @Column(name = "expiration_days", nullable = false)
    private int expirationDays;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    /**
     * Creates an empty configuration for JPA entity materialization.
     */
    protected WarningConfiguration() {
    }

    /**
     * Gets the minimum distinct reporter count required for a warning.
     *
     * @return report threshold
     */
    public int getReportThreshold() {
        return reportThreshold;
    }

    /**
     * Gets the configured warning inactivity period.
     *
     * @return expiration period in days
     */
    public int getExpirationDays() {
        return expirationDays;
    }

    /**
     * Gets the instant when the configuration was last changed.
     *
     * @return last update time
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Changes the report threshold and records the update time.
     *
     * @param reportThreshold minimum distinct reporter count
     */
    public void setReportThreshold(int reportThreshold) {
        this.reportThreshold = reportThreshold;
        this.updatedAt = Instant.now();
    }

    /**
     * Changes the warning inactivity period and records the update time.
     *
     * @param expirationDays number of days before an inactive warning expires
     */
    public void setExpirationDays(int expirationDays) {
        this.expirationDays = expirationDays;
        this.updatedAt = Instant.now();
    }
}
