package com.example.javaproject.report.service;

import com.example.javaproject.entity.CommunicationChannel;
import com.example.javaproject.entity.FraudCategory;
import com.example.javaproject.entity.PhoneNumber;
import com.example.javaproject.entity.Report;
import com.example.javaproject.entity.User;
import com.example.javaproject.entity.UserRole;
import com.example.javaproject.phonenumber.service.PhoneNumberNormalizer;
import com.example.javaproject.report.dto.CreateReportRequest;
import com.example.javaproject.repository.FraudCategoryRepository;
import com.example.javaproject.repository.PhoneNumberRepository;
import com.example.javaproject.repository.ReportRepository;
import com.example.javaproject.repository.UserRepository;
import com.example.javaproject.warning.service.WarningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private static final String EMAIL = "reporter@example.test";
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 25);

    private UserRepository userRepository;
    private PhoneNumberRepository phoneNumberRepository;
    private FraudCategoryRepository categoryRepository;
    private ReportRepository reportRepository;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        phoneNumberRepository = mock(PhoneNumberRepository.class);
        categoryRepository = mock(FraudCategoryRepository.class);
        reportRepository = mock(ReportRepository.class);
        reportService = new ReportService(
                userRepository,
                phoneNumberRepository,
                categoryRepository,
                reportRepository,
                new PhoneNumberNormalizer(),
                mock(WarningService.class),
                Clock.fixed(Instant.parse("2026-11-25T10:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void createsReportForAuthenticatedUserAndReusesNormalizedNumber() {
        User reporter = new User(EMAIL, "hash", UserRole.USER);
        FraudCategory category = new FraudCategory("POLICE", "Politseitöötaja", "Politseinikuna esinev petis");
        PhoneNumber phoneNumber = new PhoneNumber("+37251234567");
        CreateReportRequest request = new CreateReportRequest(
                "+372 5123 4567",
                "police",
                TODAY,
                null,
                CommunicationChannel.PHONE_CALL,
                " suspicious call "
        );
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(reporter));
        when(categoryRepository.findByCodeAndActiveTrue("POLICE")).thenReturn(Optional.of(category));
        when(phoneNumberRepository.findByNormalizedNumberForUpdate("+37251234567"))
                .thenReturn(Optional.of(phoneNumber));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = reportService.createReport(EMAIL, request);

        var savedReport = org.mockito.ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(savedReport.capture());
        assertThat(savedReport.getValue().getReporter()).isSameAs(reporter);
        assertThat(savedReport.getValue().getPhoneNumber()).isSameAs(phoneNumber);
        assertThat(savedReport.getValue().getCategory()).isSameAs(category);
        assertThat(savedReport.getValue().getDescription()).isEqualTo("suspicious call");
        assertThat(response.phoneNumber()).isEqualTo("+37251234567");
        assertThat(response.categoryCode()).isEqualTo("POLICE");
        verify(phoneNumberRepository, never()).save(any(PhoneNumber.class));
    }

    @Test
    void rejectsFutureIncidentDate() {
        CreateReportRequest request = new CreateReportRequest(
                "+37251234567",
                "POLICE",
                TODAY.plusDays(1),
                null,
                CommunicationChannel.PHONE_CALL,
                null
        );

        assertThatThrownBy(() -> reportService.createReport(EMAIL, request))
                .isInstanceOf(IncidentDateInFutureException.class);

        verify(userRepository, never()).findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void rejectsInactiveOrUnknownCategory() {
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(new User(EMAIL, "hash", UserRole.USER)));
        when(categoryRepository.findByCodeAndActiveTrue("POLICE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.createReport(EMAIL, new CreateReportRequest(
                "+37251234567",
                "POLICE",
                TODAY,
                null,
                CommunicationChannel.PHONE_CALL,
                null
        ))).isInstanceOf(CategoryNotAvailableException.class);

        verify(reportRepository, never()).save(any(Report.class));
    }
}
