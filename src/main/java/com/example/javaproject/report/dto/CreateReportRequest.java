package com.example.javaproject.report.dto;

import com.example.javaproject.entity.CommunicationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Input describing a suspected scam incident.
 *
 * @param phoneNumber contact number in international format
 * @param categoryCode stable code for an active report category
 * @param incidentDate date of the incident
 * @param incidentTime optional local time of the incident
 * @param communicationChannel how the contact occurred
 * @param description optional incident details without sensitive secrets
 */
@Schema(description = "Details of a suspected scam incident. Phone numbers are normalized to international E.164 format.")
public record CreateReportRequest(
        @Schema(description = "Phone number in international format, including country calling code.", example = "+372 5123 4567")
        @NotBlank @Size(max = 40) String phoneNumber,
        @Schema(description = "Code of an active category returned by GET /api/v1/categories.", example = "POLICE")
        @NotBlank @Size(max = 50) String categoryCode,
        @Schema(description = "Date the incident occurred. Future dates are not accepted.", example = "2026-11-24")
        @NotNull LocalDate incidentDate,
        @Schema(description = "Optional local incident time in HH:mm:ss format.", example = "14:30:00")
        LocalTime incidentTime,
        @Schema(description = "How the suspected scammer contacted the reporter.", example = "PHONE_CALL")
        @NotNull CommunicationChannel communicationChannel,
        @Schema(description = "Optional plain-text description. Do not include passwords, authentication codes, or other sensitive information.")
        @Size(max = 2000) String description
) {
}
