package com.example.javaproject.warning.service;

import com.example.javaproject.entity.WarningConfiguration;
import com.example.javaproject.repository.WarningConfigurationRepository;
import com.example.javaproject.warning.dto.WarningThresholdResponse;
import com.example.javaproject.warning.dto.WarningExpirationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and updates warning-generation configuration.
 */
@Service
public class WarningConfigurationService {

    private final WarningConfigurationRepository configurationRepository;
    private final WarningService warningService;

    /**
     * Creates the service with configuration storage and warning recalculation.
     *
     * @param configurationRepository persistent configuration storage
     * @param warningService warning recalculation and expiration operations
     */
    public WarningConfigurationService(
            WarningConfigurationRepository configurationRepository,
            WarningService warningService
    ) {
        this.configurationRepository = configurationRepository;
        this.warningService = warningService;
    }

    /**
     * Reads the current report threshold.
     *
     * @return threshold and configuration update time
     */
    @Transactional(readOnly = true)
    public WarningThresholdResponse getThreshold() {
        WarningConfiguration configuration = getConfiguration();
        return new WarningThresholdResponse(
                configuration.getReportThreshold(),
                configuration.getUpdatedAt()
        );
    }

    /**
     * Saves a valid report threshold and recalculates all known numbers when it changes.
     *
     * @param reportThreshold minimum distinct reporter count, at least two
     * @return saved threshold and configuration update time
     * @throws IllegalArgumentException if the threshold is less than two
     */
    @Transactional
    public WarningThresholdResponse updateThreshold(int reportThreshold) {
        if (reportThreshold < 2) {
            throw new IllegalArgumentException("Report threshold must be at least 2");
        }

        WarningConfiguration configuration = getConfiguration();
        if (configuration.getReportThreshold() != reportThreshold) {
            configuration.setReportThreshold(reportThreshold);
            warningService.recalculateAllPhoneNumbers();
        }
        return new WarningThresholdResponse(
                configuration.getReportThreshold(),
                configuration.getUpdatedAt()
        );
    }

    /**
     * Reads the current warning inactivity period.
     *
     * @return expiration days and configuration update time
     */
    @Transactional(readOnly = true)
    public WarningExpirationResponse getExpirationPeriod() {
        WarningConfiguration configuration = getConfiguration();
        return new WarningExpirationResponse(
                configuration.getExpirationDays(),
                configuration.getUpdatedAt()
        );
    }

    /**
     * Saves a valid expiration period and immediately reevaluates active warnings.
     *
     * @param expirationDays inactivity period from 1 through 3650 days
     * @return saved expiration period and configuration update time
     * @throws IllegalArgumentException if the period is outside the allowed range
     */
    @Transactional
    public WarningExpirationResponse updateExpirationPeriod(int expirationDays) {
        if (expirationDays < 1 || expirationDays > 3650) {
            throw new IllegalArgumentException("Expiration period must be between 1 and 3650 days");
        }

        WarningConfiguration configuration = getConfiguration();
        if (configuration.getExpirationDays() != expirationDays) {
            configuration.setExpirationDays(expirationDays);
            warningService.expireInactiveWarnings();
        }
        return new WarningExpirationResponse(
                configuration.getExpirationDays(),
                configuration.getUpdatedAt()
        );
    }

    private WarningConfiguration getConfiguration() {
        return configurationRepository.findById(WarningConfiguration.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Warning configuration has not been initialized"));
    }
}
