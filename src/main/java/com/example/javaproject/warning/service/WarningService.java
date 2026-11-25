package com.example.javaproject.warning.service;

import com.example.javaproject.entity.FraudCategory;
import com.example.javaproject.entity.Warning;
import com.example.javaproject.entity.WarningConfiguration;
import com.example.javaproject.entity.WarningStatus;
import com.example.javaproject.repository.FraudCategoryRepository;
import com.example.javaproject.repository.PhoneNumberRepository;
import com.example.javaproject.repository.WarningConfigurationRepository;
import com.example.javaproject.repository.WarningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculates warning eligibility, updates warning states, and expires inactive warnings.
 */
@Service
public class WarningService {

    private static final Logger logger = LoggerFactory.getLogger(WarningService.class);
    private static final EnumSet<WarningStatus> CLOSED_STATUSES =
            EnumSet.of(WarningStatus.EXPIRED, WarningStatus.WITHDRAWN, WarningStatus.THRESHOLD_NOT_MET);

    private final WarningRepository warningRepository;
    private final WarningConfigurationRepository configurationRepository;
    private final PhoneNumberRepository phoneNumberRepository;
    private final FraudCategoryRepository categoryRepository;
    private final Clock clock;

    /**
     * Creates the service with repositories and a timestamp source.
     *
     * @param warningRepository persistent warning storage
     * @param configurationRepository warning threshold and expiration settings
     * @param phoneNumberRepository phone numbers to recalculate
     * @param categoryRepository category details used for warning titles
     * @param clock source of business-operation timestamps
     */
    public WarningService(
            WarningRepository warningRepository,
            WarningConfigurationRepository configurationRepository,
            PhoneNumberRepository phoneNumberRepository,
            FraudCategoryRepository categoryRepository,
            Clock clock
    ) {
        this.warningRepository = warningRepository;
        this.configurationRepository = configurationRepository;
        this.phoneNumberRepository = phoneNumberRepository;
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    /**
     * Recalculates warning eligibility and dominant category for a phone number.
     *
     * @param phoneNumberId phone-number identifier to recalculate
     */
    @Transactional
    public void recalculateForPhoneNumber(Long phoneNumberId) {
        phoneNumberRepository.findByIdForUpdate(phoneNumberId)
                .orElseThrow(() -> new IllegalStateException("Phone number no longer exists"));
        int threshold = getThreshold();
        Instant after = latestDeactivation(phoneNumberId);
        List<WarningRepository.CategoryReportCount> counts =
                warningRepository.findCurrentReportCounts(phoneNumberId, after);
        long totalReports = counts.stream().mapToLong(WarningRepository.CategoryReportCount::getReportCount).sum();

        Warning activeWarning = warningRepository
                .findFirstByPhoneNumberIdAndStatus(phoneNumberId, WarningStatus.ACTIVE)
                .orElse(null);

        if (activeWarning != null) {
            if (totalReports < threshold) {
                activeWarning.markThresholdNotMet(clock.instant());
                logger.info(
                        "Warning deactivated because it no longer meets threshold: warningId={}, phoneNumberId={}, reportCount={}, threshold={}",
                        activeWarning.getId(),
                        phoneNumberId,
                        totalReports,
                        threshold
                );
                return;
            }
            updateDominantCategory(activeWarning, counts);
            return;
        }

        if (totalReports < threshold) {
            return;
        }

        WarningRepository.CategoryReportCount dominantCount = chooseDominantCategory(counts);
        FraudCategory dominantCategory = categoryRepository.findById(dominantCount.getCategoryId())
                .orElseThrow(() -> new IllegalStateException("Report category no longer exists"));
        var phoneNumber = phoneNumberRepository.findById(phoneNumberId)
                .orElseThrow(() -> new IllegalStateException("Phone number no longer exists"));
        Warning warning = warningRepository.save(new Warning(
                phoneNumber,
                dominantCategory,
                dominantCategory.getWarningTitle(),
                clock.instant()
        ));
        logger.info(
                "Warning created: warningId={}, phoneNumberId={}, categoryId={}, reportCount={}",
                warning.getId(),
                phoneNumberId,
                dominantCategory.getId(),
                totalReports
        );
    }

    /**
     * Recalculates warning eligibility for all known phone numbers.
     */
    @Transactional
    public void recalculateAllPhoneNumbers() {
        for (Long phoneNumberId : phoneNumberRepository.findAllIds()) {
            recalculateForPhoneNumber(phoneNumberId);
        }
    }

    /**
     * Expires inactive warnings and deactivates warnings that no longer meet the threshold.
     *
     * @return number of warnings whose status changed
     */
    @Transactional
    public int expireInactiveWarnings() {
        Instant now = clock.instant();
        int expirationDays = getExpirationDays();
        int threshold = getThreshold();
        int updatedWarnings = 0;

        for (Long warningId : warningRepository.findIdsByStatus(WarningStatus.ACTIVE)) {
            Warning warning = warningRepository.findById(warningId)
                    .orElseThrow(() -> new IllegalStateException("Warning no longer exists"));
            Long phoneNumberId = warning.getPhoneNumber().getId();
            phoneNumberRepository.findByIdForUpdate(phoneNumberId)
                    .orElseThrow(() -> new IllegalStateException("Phone number no longer exists"));
            Warning lockedWarning = warningRepository.findByIdForUpdate(warningId)
                    .orElseThrow(() -> new IllegalStateException("Warning no longer exists"));
            if (lockedWarning.getStatus() != WarningStatus.ACTIVE) {
                continue;
            }

            List<WarningRepository.CategoryReportCount> counts = warningRepository.findCurrentReportCounts(
                    phoneNumberId,
                    latestDeactivation(phoneNumberId)
            );
            long totalReports = counts.stream()
                    .mapToLong(WarningRepository.CategoryReportCount::getReportCount)
                    .sum();
            if (totalReports < threshold) {
                lockedWarning.markThresholdNotMet(now);
                updatedWarnings++;
                logger.info(
                        "Warning deactivated because it no longer meets threshold: warningId={}, phoneNumberId={}, reportCount={}, threshold={}",
                        warningId,
                        phoneNumberId,
                        totalReports,
                        threshold
                );
                continue;
            }

            Instant latestReportAt = counts.stream()
                    .map(WarningRepository.CategoryReportCount::getLatestReportAt)
                    .max(Instant::compareTo)
                    .orElseThrow(() -> new IllegalStateException("Active warning has no qualifying reports"));
            if (!latestReportAt.isAfter(now.minus(Duration.ofDays(expirationDays)))) {
                lockedWarning.expire(now);
                updatedWarnings++;
                logger.info(
                        "Warning expired: warningId={}, phoneNumberId={}, expirationDays={}",
                        warningId,
                        phoneNumberId,
                        expirationDays
                );
            }
        }
        return updatedWarnings;
    }

    private int getThreshold() {
        return getConfiguration().getReportThreshold();
    }

    private int getExpirationDays() {
        return getConfiguration().getExpirationDays();
    }

    private WarningConfiguration getConfiguration() {
        return configurationRepository.findById(WarningConfiguration.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Warning configuration has not been initialized"));
    }

    private Instant latestDeactivation(Long phoneNumberId) {
        return warningRepository.findFirstByPhoneNumberIdAndStatusInOrderByDeactivatedAtDesc(
                        phoneNumberId,
                        CLOSED_STATUSES
                )
                .map(Warning::getDeactivatedAt)
                .orElse(null);
    }

    private void updateDominantCategory(
            Warning warning,
            List<WarningRepository.CategoryReportCount> counts
    ) {
        Map<Long, WarningRepository.CategoryReportCount> countsByCategory = counts.stream()
                .collect(Collectors.toMap(
                        WarningRepository.CategoryReportCount::getCategoryId,
                        Function.identity()
                ));
        WarningRepository.CategoryReportCount current = countsByCategory.get(
                warning.getDominantCategory().getId()
        );
        WarningRepository.CategoryReportCount dominant = chooseDominantCategory(counts);
        long currentCount = current == null ? 0 : current.getReportCount();
        if (dominant.getReportCount() > currentCount) {
            FraudCategory category = categoryRepository.findById(dominant.getCategoryId())
                    .orElseThrow(() -> new IllegalStateException("Report category no longer exists"));
            Long previousCategoryId = warning.getDominantCategory().getId();
            warning.updateDominantCategory(category);
            logger.info(
                    "Warning dominant category changed: warningId={}, previousCategoryId={}, categoryId={}",
                    warning.getId(),
                    previousCategoryId,
                    category.getId()
            );
        }
    }

    private WarningRepository.CategoryReportCount chooseDominantCategory(
            List<WarningRepository.CategoryReportCount> counts
    ) {
        return counts.stream()
                .max(Comparator
                        .comparingLong(WarningRepository.CategoryReportCount::getReportCount)
                        .thenComparing(
                                WarningRepository.CategoryReportCount::getFirstReportAt,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                WarningRepository.CategoryReportCount::getFirstReportId,
                                Comparator.reverseOrder()
                        ))
                .orElseThrow(() -> new IllegalStateException("Cannot choose a category without reports"));
    }
}
