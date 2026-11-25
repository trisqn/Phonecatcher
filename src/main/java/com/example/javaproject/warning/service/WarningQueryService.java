package com.example.javaproject.warning.service;

import com.example.javaproject.entity.WarningStatus;
import com.example.javaproject.entity.Warning;
import com.example.javaproject.repository.WarningRepository;
import com.example.javaproject.warning.dto.WarningResponse;
import com.example.javaproject.warning.dto.WarningSyncResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Queries public warning data and synchronization snapshots.
 */
@Service
public class WarningQueryService {

    private final WarningRepository warningRepository;
    private final Clock clock;

    /**
     * Creates the query service with its repository and timestamp source.
     *
     * @param warningRepository persistent warning storage
     * @param clock source of server synchronization timestamps
     */
    public WarningQueryService(WarningRepository warningRepository, Clock clock) {
        this.warningRepository = warningRepository;
        this.clock = clock;
    }

    /**
     * Loads active warnings for public lookup.
     *
     * @return active warnings ordered by activation time
     */
    @Transactional(readOnly = true)
    public List<WarningResponse> getActiveWarnings() {
        return warningRepository.findAllByStatusOrderByActivatedAtDesc(WarningStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Produces a warning snapshot or incremental synchronization response.
     *
     * @param updatedAfter previous server sync cursor, or {@code null} for initial sync
     * @return warning items and the current server cursor
     * @throws InvalidSyncTimeException if the supplied cursor is in the future
     */
    @Transactional(readOnly = true)
    public WarningSyncResponse getWarningsForSync(Instant updatedAfter) {
        Instant syncTime = clock.instant();
        if (updatedAfter != null && updatedAfter.isAfter(syncTime)) {
            throw new InvalidSyncTimeException();
        }
        List<WarningResponse> items;
        if (updatedAfter == null) {
            items = warningRepository
                    .findAllByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAscIdAsc(
                            WarningStatus.ACTIVE,
                            syncTime
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else {
            items = warningRepository
                    .findAllByUpdatedAtGreaterThanAndUpdatedAtLessThanEqualOrderByUpdatedAtAscIdAsc(
                            updatedAfter,
                            syncTime
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return new WarningSyncResponse(items, syncTime);
    }

    private WarningResponse toResponse(Warning warning) {
        return new WarningResponse(
                warning.getId(),
                warning.getPhoneNumber().getNormalizedNumber(),
                warning.getDominantCategory().getCode(),
                warning.getDominantCategory().getName(),
                warning.getTitle(),
                warning.getStatus(),
                warning.getActivatedAt(),
                warning.getUpdatedAt()
        );
    }
}
