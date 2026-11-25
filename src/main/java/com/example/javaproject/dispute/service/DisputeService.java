package com.example.javaproject.dispute.service;

import com.example.javaproject.entity.Dispute;
import com.example.javaproject.entity.DisputeStatus;
import com.example.javaproject.entity.WarningStatus;
import com.example.javaproject.repository.DisputeRepository;
import com.example.javaproject.repository.UserRepository;
import com.example.javaproject.repository.WarningRepository;
import com.example.javaproject.dispute.dto.DisputeResponse;
import com.example.javaproject.dispute.dto.ReviewDisputeRequest;
import com.example.javaproject.dispute.service.InvalidDisputeDecisionException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.List;

/**
 * Implements dispute submission and moderator review workflows.
 */
@Service
public class DisputeService {

    private static final Logger logger = LoggerFactory.getLogger(DisputeService.class);
    private static final String OPEN_DISPUTE_INDEX = "ux_dispute_submitted_warning_submitter";

    private final DisputeRepository disputeRepository;
    private final WarningRepository warningRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    /**
     * Creates the service with its repositories and clock.
     *
     * @param disputeRepository persistent dispute storage
     * @param warningRepository persistent warning storage
     * @param userRepository persistent account storage
     * @param clock source of timestamps for dispute operations
     */
    public DisputeService(
            DisputeRepository disputeRepository,
            WarningRepository warningRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.disputeRepository = disputeRepository;
        this.warningRepository = warningRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /**
     * Submits a dispute for an active warning.
     *
     * @param warningId warning being disputed
     * @param submitterEmail authenticated submitter's email
     * @param reason plain-text explanation
     * @return persisted dispute
     * @throws WarningNotFoundException if the warning does not exist
     * @throws WarningNotActiveException if the warning is not active
     * @throws DuplicateOpenDisputeException if this user already has an open dispute for the warning
     */
    @Transactional
    public DisputeResponse submitDispute(Long warningId, String submitterEmail, String reason) {
        var warning = warningRepository.findByIdForUpdate(warningId)
                .orElseThrow(() -> new WarningNotFoundException(warningId));
        if (warning.getStatus() != WarningStatus.ACTIVE) {
            throw new WarningNotActiveException();
        }

        var submitter = userRepository.findByEmailIgnoreCase(submitterEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
        if (disputeRepository.existsByWarningIdAndSubmitterIdAndStatus(
                warningId,
                submitter.getId(),
                DisputeStatus.SUBMITTED
        )) {
            throw new DuplicateOpenDisputeException();
        }

        Dispute dispute = new Dispute(warning, submitter, reason.trim());
        try {
            disputeRepository.saveAndFlush(dispute);
        } catch (DataIntegrityViolationException exception) {
            if (isOpenDisputeConflict(exception)) {
                throw new DuplicateOpenDisputeException();
            }
            throw exception;
        }
        logger.info(
                "Dispute submitted: disputeId={}, warningId={}, submitterId={}",
                dispute.getId(),
                warningId,
                submitter.getId()
        );
        return toResponse(dispute);
    }

    /**
     * Retrieves disputes currently awaiting a moderation decision.
     *
     * @return submitted disputes ordered by submission time
     */
    @Transactional(readOnly = true)
    public List<DisputeResponse> getSubmittedDisputes() {
        return disputeRepository.findAllByStatusOrderBySubmittedAtAsc(DisputeStatus.SUBMITTED).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Reviews a submitted dispute and withdraws its warning if accepted.
     *
     * @param disputeId dispute being reviewed
     * @param reviewerEmail authenticated reviewer email
     * @param request decision and review comment
     * @return reviewed dispute
     * @throws DisputeNotFoundException if the dispute does not exist
     * @throws DisputeAlreadyReviewedException if a decision has already been recorded
     */
    @Transactional
    public DisputeResponse reviewDispute(
            Long disputeId,
            String reviewerEmail,
            ReviewDisputeRequest request
    ) {
        var existingDispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException(disputeId));
        Long warningId = existingDispute.getWarning().getId();
        var warning = warningRepository.findByIdForUpdate(warningId)
                .orElseThrow(() -> new WarningNotFoundException(warningId));
        var dispute = disputeRepository.findByIdForUpdate(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException(disputeId));
        if (dispute.getStatus() != DisputeStatus.SUBMITTED) {
            throw new DisputeAlreadyReviewedException();
        }

        var reviewer = userRepository.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
        DisputeStatus decision;
        try {
            decision = DisputeStatus.valueOf(request.decision());
        } catch (IllegalArgumentException exception) {
            throw new InvalidDisputeDecisionException();
        }

        dispute.review(reviewer, decision, request.reviewComment().trim(), clock.instant());
        if (decision == DisputeStatus.ACCEPTED) {
            if (warning.getStatus() != WarningStatus.ACTIVE) {
                throw new WarningNotActiveException();
            }
            warning.withdraw(clock.instant());
        }
        logger.info(
                "Dispute reviewed: disputeId={}, warningId={}, reviewerId={}, decision={}",
                disputeId,
                warningId,
                reviewer.getId(),
                decision
        );
        return toResponse(dispute);
    }

    private DisputeResponse toResponse(Dispute dispute) {
        return new DisputeResponse(
                dispute.getId(),
                dispute.getWarning().getId(),
                dispute.getWarning().getPhoneNumber().getNormalizedNumber(),
                dispute.getSubmitter().getId(),
                dispute.getReason(),
                dispute.getStatus(),
                dispute.getReviewer() == null ? null : dispute.getReviewer().getId(),
                dispute.getReviewComment(),
                dispute.getSubmittedAt(),
                dispute.getReviewedAt()
        );
    }

    private boolean isOpenDisputeConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && OPEN_DISPUTE_INDEX.equals(constraintViolation.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
