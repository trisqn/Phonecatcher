package com.example.javaproject.report.dto;

import com.example.javaproject.entity.CommunicationChannel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Report details returned after submission.
 *
 * @param id report identifier
 * @param phoneNumber normalized phone number
 * @param categoryCode stable category code
 * @param incidentDate date of the incident
 * @param incidentTime local incident time, if supplied
 * @param communicationChannel how the contact occurred
 * @param description optional incident details
 * @param createdAt report creation instant
 */
public record ReportResponse(
        Long id,
        String phoneNumber,
        String categoryCode,
        LocalDate incidentDate,
        LocalTime incidentTime,
        CommunicationChannel communicationChannel,
        String description,
        Instant createdAt
) {
}
