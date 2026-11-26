package com.example.javaproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A configurable category used to classify submitted fraud reports.
 */
@Entity
@Table(
        name = "fraud_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fraud_category_code",
                columnNames = "code"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FraudCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "warning_title", nullable = false, length = 200)
    private String warningTitle;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Creates an active category.
     *
     * @param code unique stable category code
     * @param name display name
     * @param warningTitle title displayed on warnings for this category
     */
    public FraudCategory(String code, String name, String warningTitle) {
        this.code = code;
        this.name = name;
        this.warningTitle = warningTitle;
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
     * Updates editable display fields and the category's availability.
     *
     * @param name display name
     * @param warningTitle warning title used for this category
     * @param active whether new reports may use the category
     */
    public void update(String name, String warningTitle, boolean active) {
        this.name = name;
        this.warningTitle = warningTitle;
        this.active = active;
    }

    /**
     * Prevents new reports from using this category without deleting history.
     */
    public void deactivate() {
        this.active = false;
    }

}
