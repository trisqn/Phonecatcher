package com.example.javaproject.repository;

import com.example.javaproject.entity.Dispute;
import com.example.javaproject.entity.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for disputes, including locked moderation reads.
 */
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    /**
     * Checks for an open dispute from a user for a particular warning.
     *
     * @param warningId warning identifier
     * @param submitterId user identifier
     * @param status dispute state to check
     * @return whether a matching dispute exists
     */
    boolean existsByWarningIdAndSubmitterIdAndStatus(
            Long warningId,
            Long submitterId,
            DisputeStatus status
    );

    /**
     * Retrieves disputes in a given state with relations needed for response mapping.
     *
     * @param status dispute state to retrieve
     * @return matching disputes ordered by submission time
     */
    @EntityGraph(attributePaths = {"warning", "warning.phoneNumber", "submitter"})
    List<Dispute> findAllByStatusOrderBySubmittedAtAsc(DisputeStatus status);

    /**
     * Locks a dispute row while a moderator records a decision.
     *
     * @param id dispute identifier
     * @return the locked dispute, if it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select dispute from Dispute dispute where dispute.id = :id")
    Optional<Dispute> findByIdForUpdate(@Param("id") Long id);
}
