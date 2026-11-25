package com.example.javaproject.report.service;

import com.example.javaproject.entity.PhoneNumber;
import com.example.javaproject.entity.Report;
import com.example.javaproject.report.dto.CreateReportRequest;
import com.example.javaproject.report.dto.ReportResponse;
import com.example.javaproject.phonenumber.service.PhoneNumberNormalizer;
import com.example.javaproject.repository.FraudCategoryRepository;
import com.example.javaproject.repository.PhoneNumberRepository;
import com.example.javaproject.repository.ReportRepository;
import com.example.javaproject.repository.UserRepository;
import com.example.javaproject.warning.service.WarningService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Validates and persists reports and recalculates associated warnings.
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final UserRepository userRepository;
    private final PhoneNumberRepository phoneNumberRepository;
    private final FraudCategoryRepository categoryRepository;
    private final ReportRepository reportRepository;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final WarningService warningService;
    private final Clock clock;

    /**
     * Creates the service with its repositories and report-processing collaborators.
     *
     * @param userRepository persistent account storage
     * @param phoneNumberRepository persistent phone-number storage
     * @param categoryRepository persistent category storage
     * @param reportRepository persistent report storage
     * @param phoneNumberNormalizer phone-number validation and normalization
     * @param warningService warning recalculation operations
     * @param clock source of report timestamps and date validation
     */
    public ReportService(
            UserRepository userRepository,
            PhoneNumberRepository phoneNumberRepository,
            FraudCategoryRepository categoryRepository,
            ReportRepository reportRepository,
            PhoneNumberNormalizer phoneNumberNormalizer,
            WarningService warningService,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.phoneNumberRepository = phoneNumberRepository;
        this.categoryRepository = categoryRepository;
        this.reportRepository = reportRepository;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
        this.warningService = warningService;
        this.clock = clock;
    }

    /**
     * Persists a report and recalculates its phone number's warning atomically.
     *
     * @param reporterEmail email of the authenticated reporter
     * @param request incident details
     * @return the created report
     * @throws IncidentDateInFutureException if the incident date is in the future
     * @throws CategoryNotAvailableException if the category is missing or inactive
     * @throws com.example.javaproject.phonenumber.service.InvalidPhoneNumberException if the number is invalid
     */
    @Transactional
    public ReportResponse createReport(String reporterEmail, CreateReportRequest request) {
        LocalDate today = LocalDate.now(clock);
        if (request.incidentDate().isAfter(today)) {
            throw new IncidentDateInFutureException();
        }

        var reporter = userRepository.findByEmailIgnoreCase(reporterEmail)
                .orElseThrow(ReporterNotFoundException::new);
        String categoryCode = request.categoryCode().trim().toUpperCase(Locale.ROOT);
        var category = categoryRepository.findByCodeAndActiveTrue(categoryCode)
                .orElseThrow(() -> new CategoryNotAvailableException(categoryCode));

        String normalizedNumber = phoneNumberNormalizer.normalize(request.phoneNumber());
        PhoneNumber phoneNumber = phoneNumberRepository.findByNormalizedNumberForUpdate(normalizedNumber)
                .orElseGet(() -> phoneNumberRepository.saveAndFlush(new PhoneNumber(normalizedNumber)));

        Report report = reportRepository.save(new Report(
                reporter,
                phoneNumber,
                category,
                request.incidentDate(),
                request.incidentTime(),
                request.communicationChannel(),
                request.description() == null ? null : request.description().trim()
        ));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        logger.info(
                "Report created: reportId={}, reporterId={}, phoneNumberId={}, categoryId={}",
                report.getId(),
                reporter.getId(),
                phoneNumber.getId(),
                category.getId()
        );

        return new ReportResponse(
                report.getId(),
                phoneNumber.getNormalizedNumber(),
                category.getCode(),
                report.getIncidentDate(),
                report.getIncidentTime(),
                report.getCommunicationChannel(),
                report.getDescription(),
                report.getCreatedAt()
        );
    }
}
