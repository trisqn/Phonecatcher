package com.example.javaproject.warning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the scheduled inactivity-expiration check for active warnings.
 */
@Component
public class WarningExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WarningExpirationScheduler.class);
    private final WarningService warningService;

    /**
     * Creates the scheduler with its warning service.
     *
     * @param warningService warning expiration operation
     */
    public WarningExpirationScheduler(WarningService warningService) {
        this.warningService = warningService;
    }

    /**
     * Runs the daily warning inactivity check and records its result.
     */
    @Scheduled(fixedDelay = 86_400_000L)
    public void expireInactiveWarnings() {
        logger.info("Warning expiration job started");
        int updatedWarnings = warningService.expireInactiveWarnings();
        logger.info("Warning expiration job completed: updatedWarnings={}", updatedWarnings);
    }
}
