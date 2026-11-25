package com.example.javaproject.dispute.service;

import com.example.javaproject.PostgresIntegrationTestBase;
import com.example.javaproject.dispute.dto.ReviewDisputeRequest;
import com.example.javaproject.entity.CommunicationChannel;
import com.example.javaproject.entity.DisputeStatus;
import com.example.javaproject.entity.FraudCategory;
import com.example.javaproject.entity.PhoneNumber;
import com.example.javaproject.entity.Report;
import com.example.javaproject.entity.User;
import com.example.javaproject.entity.UserRole;
import com.example.javaproject.entity.WarningStatus;
import com.example.javaproject.repository.DisputeRepository;
import com.example.javaproject.repository.FraudCategoryRepository;
import com.example.javaproject.repository.PhoneNumberRepository;
import com.example.javaproject.repository.ReportRepository;
import com.example.javaproject.repository.UserRepository;
import com.example.javaproject.repository.WarningRepository;
import com.example.javaproject.warning.service.WarningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DisputeServiceIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private WarningService warningService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired
    private FraudCategoryRepository categoryRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private WarningRepository warningRepository;

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void acceptsDisputeAndWithdrawsItsActiveWarning() {
        var warning = createActiveWarning("+37255599991", "dispute-accept");
        User submitter = saveUser("dispute-accept-submitter@example.test", UserRole.USER);
        User moderator = saveUser("dispute-accept-moderator@example.test", UserRole.MODERATOR);

        var submitted = disputeService.submitDispute(
                warning.getId(),
                submitter.getEmail(),
                "I believe this warning concerns a legitimate call."
        );
        var reviewed = disputeService.reviewDispute(
                submitted.id(),
                moderator.getEmail(),
                new ReviewDisputeRequest("ACCEPTED", "Evidence supports the dispute.")
        );

        assertThat(reviewed.status()).isEqualTo(DisputeStatus.ACCEPTED);
        assertThat(reviewed.reviewerId()).isEqualTo(moderator.getId());
        assertThat(reviewed.reviewComment()).isEqualTo("Evidence supports the dispute.");
        assertThat(reviewed.reviewedAt()).isNotNull();
        assertThat(warningRepository.findById(warning.getId()).orElseThrow().getStatus())
                .isEqualTo(WarningStatus.WITHDRAWN);
    }

    @Test
    void rejectedDisputeKeepsWarningActiveAndCannotBeReviewedAgain() {
        var warning = createActiveWarning("+37255599992", "dispute-reject");
        User submitter = saveUser("dispute-reject-submitter@example.test", UserRole.USER);
        User moderator = saveUser("dispute-reject-moderator@example.test", UserRole.MODERATOR);

        var submitted = disputeService.submitDispute(warning.getId(), submitter.getEmail(), "Please review.");
        var reviewed = disputeService.reviewDispute(
                submitted.id(),
                moderator.getEmail(),
                new ReviewDisputeRequest("REJECTED", "The report threshold remains valid.")
        );

        assertThat(reviewed.status()).isEqualTo(DisputeStatus.REJECTED);
        assertThat(warningRepository.findById(warning.getId()).orElseThrow().getStatus())
                .isEqualTo(WarningStatus.ACTIVE);
        assertThatThrownBy(() -> disputeService.reviewDispute(
                submitted.id(),
                moderator.getEmail(),
                new ReviewDisputeRequest("ACCEPTED", "Second decision.")
        )).isInstanceOf(DisputeAlreadyReviewedException.class);
    }

    @Test
    void userCannotHaveTwoSubmittedDisputesForOneWarning() {
        var warning = createActiveWarning("+37255599993", "dispute-duplicate");
        User submitter = saveUser("dispute-duplicate-submitter@example.test", UserRole.USER);
        disputeService.submitDispute(warning.getId(), submitter.getEmail(), "First reason.");

        assertThatThrownBy(() -> disputeService.submitDispute(
                warning.getId(),
                submitter.getEmail(),
                "Another reason."
        )).isInstanceOf(DuplicateOpenDisputeException.class);
        assertThat(disputeRepository.findAllByStatusOrderBySubmittedAtAsc(DisputeStatus.SUBMITTED))
                .hasSize(1);
    }

    @Test
    void regularUserCannotAccessModerationQueue() throws Exception {
        mockMvc.perform(get("/api/v1/moderation/disputes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void moderatorQueueIsForbiddenForRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/moderation/disputes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void moderatorCanReadSubmittedDisputes() throws Exception {
        mockMvc.perform(get("/api/v1/moderation/disputes"))
                .andExpect(status().isOk());
    }

    private com.example.javaproject.entity.Warning createActiveWarning(String number, String suffix) {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(new PhoneNumber(number));
        FraudCategory category = categoryRepository.findByCode("POLICE").orElseThrow();
        reportRepository.saveAndFlush(new Report(
                saveUser(suffix + "-one@example.test", UserRole.USER),
                phoneNumber,
                category,
                LocalDate.now().minusDays(1),
                null,
                CommunicationChannel.PHONE_CALL,
                null
        ));
        reportRepository.saveAndFlush(new Report(
                saveUser(suffix + "-two@example.test", UserRole.USER),
                phoneNumber,
                category,
                LocalDate.now().minusDays(1),
                null,
                CommunicationChannel.PHONE_CALL,
                null
        ));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        return warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        ).orElseThrow();
    }

    private User saveUser(String email, UserRole role) {
        return userRepository.saveAndFlush(new User(email, "hash", role));
    }
}
