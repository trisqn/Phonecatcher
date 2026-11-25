package com.example.javaproject.repository;

import com.example.javaproject.entity.Warning;
import com.example.javaproject.entity.WarningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for warnings and their report-derived aggregates.
 */
public interface WarningRepository extends JpaRepository<Warning, Long> {

    /**
     * Lists warnings of a state with their phone and category details loaded.
     *
     * @param status warning state to retrieve
     * @return matching warnings ordered by activation time
     */
    @EntityGraph(attributePaths = {"phoneNumber", "dominantCategory"})
    List<Warning> findAllByStatusOrderByActivatedAtDesc(WarningStatus status);

    /**
     * Lists warnings of a state updated no later than the initial sync time.
     *
     * @param status warning state to retrieve
     * @param syncTime inclusive upper timestamp for the initial snapshot
     * @return matching warnings in stable update order
     */
    @EntityGraph(attributePaths = {"phoneNumber", "dominantCategory"})
    List<Warning> findAllByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAscIdAsc(
            WarningStatus status,
            Instant syncTime
    );

    /**
     * Lists warnings changed inside an incremental synchronization window.
     *
     * @param updatedAfter exclusive lower timestamp of the incremental window
     * @param syncTime inclusive upper timestamp of the incremental window
     * @return warnings changed within the synchronization window
     */
    @EntityGraph(attributePaths = {"phoneNumber", "dominantCategory"})
    List<Warning> findAllByUpdatedAtGreaterThanAndUpdatedAtLessThanEqualOrderByUpdatedAtAscIdAsc(
            Instant updatedAfter,
            Instant syncTime
    );

    /**
     * Finds the warning in a given state for a phone number.
     *
     * @param phoneNumberId phone-number identifier
     * @param status warning state to find
     * @return the matching warning, if present
     */
    Optional<Warning> findFirstByPhoneNumberIdAndStatus(
            Long phoneNumberId,
            WarningStatus status
    );

    /**
     * Gets warning identifiers for a state without loading entity relations.
     *
     * @param status warning state to retrieve
     * @return warning identifiers in ascending order
     */
    @Query("select warning.id from Warning warning where warning.status = :status order by warning.id")
    List<Long> findIdsByStatus(@Param("status") WarningStatus status);

    /**
     * Locks a warning row while state-changing business logic runs.
     *
     * @param id warning identifier
     * @return the locked warning, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select warning from Warning warning where warning.id = :id")
    Optional<Warning> findByIdForUpdate(@Param("id") Long id);

    /**
     * Finds the latest deactivation among the supplied terminal states.
     *
     * @param phoneNumberId phone-number identifier
     * @param statuses terminal states to search
     * @return the warning with the latest deactivation, if present
     */
    Optional<Warning> findFirstByPhoneNumberIdAndStatusInOrderByDeactivatedAtDesc(
            Long phoneNumberId,
            Collection<WarningStatus> statuses
    );

    /**
     * Counts each reporter's latest report in the current warning cycle by category.
     *
     * @param phoneNumberId target phone number
     * @param after exclusive lower time bound, or {@code null} for all reports
     * @return counts and report timestamps used by warning calculations
     */
    @Query(value = """
            SELECT latest.category_id AS categoryId,
                   COUNT(*) AS reportCount,
                   MIN(latest.created_at) AS firstReportAt,
                   MIN(latest.id) AS firstReportId,
                   MAX(latest.created_at) AS latestReportAt
            FROM (
                SELECT DISTINCT ON (reporter_id) id, reporter_id, category_id, created_at
                FROM report
                WHERE phone_number_id = :phoneNumberId
                  AND (CAST(:after AS timestamptz) IS NULL OR created_at > :after)
                ORDER BY reporter_id, created_at DESC, id DESC
            ) latest
            GROUP BY latest.category_id
            """, nativeQuery = true)
    List<CategoryReportCount> findCurrentReportCounts(
            @Param("phoneNumberId") Long phoneNumberId,
            @Param("after") Instant after
    );

    /**
     * Projection of per-category report counts used in warning recalculation.
     */
    interface CategoryReportCount {
        /**
         * Gets the category identifier.
         *
         * @return category identifier
         */
        Long getCategoryId();

        /**
         * Gets the number of distinct reporters represented by this category.
         *
         * @return distinct reporter count
         */
        Long getReportCount();

        /**
         * Gets the earliest counted report timestamp for deterministic tie-breaking.
         *
         * @return earliest counted report time
         */
        Instant getFirstReportAt();

        /**
         * Gets the earliest counted report identifier for deterministic tie-breaking.
         *
         * @return earliest report identifier
         */
        Long getFirstReportId();

        /**
         * Gets the most recent counted report timestamp.
         *
         * @return most recent report time
         */
        Instant getLatestReportAt();
    }
}
