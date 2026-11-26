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
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A user-submitted account of a suspected scam incident.
 */
@Entity
@Table(
        name = "report",
        indexes = {
                @Index(name = "ix_report_phone_number_id", columnList = "phone_number_id"),
                @Index(name = "ix_report_reporter_id", columnList = "reporter_id"),
                @Index(name = "ix_report_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_number_id", nullable = false)
    private PhoneNumber phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FraudCategory category;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_time")
    private LocalTime incidentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_channel", nullable = false, length = 30)
    private CommunicationChannel communicationChannel;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Creates a report with the incident details supplied by its reporter.
     *
     * @param reporter account submitting the report
     * @param phoneNumber normalized phone number involved
     * @param category selected fraud category
     * @param incidentDate date the incident occurred
     * @param incidentTime local time the incident occurred, if known
     * @param communicationChannel method used to contact the reporter
     * @param description optional incident description
     */
    public Report(
            User reporter,
            PhoneNumber phoneNumber,
            FraudCategory category,
            LocalDate incidentDate,
            LocalTime incidentTime,
            CommunicationChannel communicationChannel,
            String description
    ) {
        this.reporter = reporter;
        this.phoneNumber = phoneNumber;
        this.category = category;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.communicationChannel = communicationChannel;
        this.description = description;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

}
