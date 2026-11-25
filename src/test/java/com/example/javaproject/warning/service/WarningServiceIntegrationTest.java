package com.example.javaproject.warning.service;

import com.example.javaproject.PostgresIntegrationTestBase;
import com.example.javaproject.entity.CommunicationChannel;
import com.example.javaproject.entity.FraudCategory;
import com.example.javaproject.entity.PhoneNumber;
import com.example.javaproject.entity.Report;
import com.example.javaproject.entity.User;
import com.example.javaproject.entity.UserRole;
import com.example.javaproject.entity.WarningStatus;
import com.example.javaproject.repository.FraudCategoryRepository;
import com.example.javaproject.repository.PhoneNumberRepository;
import com.example.javaproject.repository.ReportRepository;
import com.example.javaproject.repository.UserRepository;
import com.example.javaproject.repository.WarningRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.Instant;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WarningServiceIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private WarningService warningService;

    @Autowired
    private WarningConfigurationService configurationService;

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
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser(roles = "USER")
    void thresholdSettingsAreForbiddenForRegularUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/warning-threshold"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanReadCurrentThreshold() throws Exception {
        mockMvc.perform(get("/api/v1/admin/warning-threshold"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void expirationSettingsAreForbiddenForRegularUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/warning-threshold/expiration"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanReadExpirationPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/admin/warning-threshold/expiration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDays").value(60));
    }

    @Test
    void activeWarningsArePublicAndDoNotExposeReportDetails() throws Exception {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(
                new PhoneNumber("+37255512347")
        );
        FraudCategory police = categoryRepository.findByCode("POLICE").orElseThrow();
        User firstReporter = saveUser("public-warning-first@example.test");
        User secondReporter = saveUser("public-warning-second@example.test");
        reportRepository.saveAndFlush(report(firstReporter, phoneNumber, police));
        reportRepository.saveAndFlush(new Report(
                secondReporter,
                phoneNumber,
                police,
                LocalDate.now().minusDays(1),
                null,
                CommunicationChannel.PHONE_CALL,
                "private report description"
        ));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());

        mockMvc.perform(get("/api/v1/warnings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].phoneNumber").value("+37255512347"))
                .andExpect(jsonPath("$[0].categoryCode").value("POLICE"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].reporter").doesNotExist())
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    void warningSyncIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/sync/warnings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.syncTime").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void firstWarningSyncReturnsOnlyActiveWarningsAndServerSyncTime() throws Exception {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(
                new PhoneNumber("+37255512348")
        );
        FraudCategory police = categoryRepository.findByCode("POLICE").orElseThrow();
        reportRepository.saveAndFlush(report(saveUser("sync-first@example.test"), phoneNumber, police));
        reportRepository.saveAndFlush(report(saveUser("sync-second@example.test"), phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());

        mockMvc.perform(get("/api/v1/sync/warnings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].phoneNumber")
                        .value(org.hamcrest.Matchers.hasItem("+37255512348")))
                .andExpect(jsonPath("$.items[?(@.phoneNumber == '+37255512348')].status")
                        .value(org.hamcrest.Matchers.contains("ACTIVE")))
                .andExpect(jsonPath("$.syncTime").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void incrementalSyncReturnsUpdatedInactiveWarnings() throws Exception {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(
                new PhoneNumber("+37255512349")
        );
        FraudCategory police = categoryRepository.findByCode("POLICE").orElseThrow();
        reportRepository.saveAndFlush(report(saveUser("sync-delta-first@example.test"), phoneNumber, police));
        reportRepository.saveAndFlush(report(saveUser("sync-delta-second@example.test"), phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());

        var warning = warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        ).orElseThrow();
        warning.markThresholdNotMet(Instant.now());
        warningRepository.saveAndFlush(warning);

        mockMvc.perform(get("/api/v1/sync/warnings").param("updatedAfter", Instant.EPOCH.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id")
                        .value(org.hamcrest.Matchers.hasItem(warning.getId().intValue())))
                .andExpect(jsonPath("$.items[*].status")
                        .value(org.hamcrest.Matchers.hasItem("THRESHOLD_NOT_MET")))
                .andExpect(jsonPath("$.syncTime").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void incrementalSyncRejectsFutureCursor() throws Exception {
        mockMvc.perform(get("/api/v1/sync/warnings")
                        .param("updatedAfter", Instant.now().plusSeconds(3600).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SYNC_TIME"));
    }

    @Test
    void createsWarningOnlyForDistinctUsersAndAppliesNewThresholdToActiveWarnings() {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(
                new PhoneNumber("+37255512345")
        );
        FraudCategory police = categoryRepository.findByCode("POLICE").orElseThrow();
        FraudCategory bank = categoryRepository.findByCode("BANK_EMPLOYEE").orElseThrow();
        User firstReporter = userRepository.saveAndFlush(
                new User("warning-first@example.test", "hash", UserRole.USER)
        );
        User secondReporter = userRepository.saveAndFlush(
                new User("warning-second@example.test", "hash", UserRole.USER)
        );

        reportRepository.saveAndFlush(report(firstReporter, phoneNumber, police));
        reportRepository.saveAndFlush(report(firstReporter, phoneNumber, bank));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());

        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        )).isEmpty();

        reportRepository.saveAndFlush(report(secondReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        var warning = warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        ).orElseThrow();
        assertThat(warning.getDominantCategory().getCode()).isEqualTo("BANK_EMPLOYEE");

        configurationService.updateThreshold(3);
        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.THRESHOLD_NOT_MET
        )).isPresent();

        User thirdReporter = userRepository.saveAndFlush(
                new User("warning-third@example.test", "hash", UserRole.USER)
        );
        reportRepository.saveAndFlush(report(thirdReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        )).isEmpty();

        User fourthReporter = userRepository.saveAndFlush(
                new User("warning-fourth@example.test", "hash", UserRole.USER)
        );
        reportRepository.saveAndFlush(report(fourthReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        )).isEmpty();

        User fifthReporter = userRepository.saveAndFlush(
                new User("warning-fifth@example.test", "hash", UserRole.USER)
        );
        reportRepository.saveAndFlush(report(fifthReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        )).isPresent();
    }

    @Test
    void changesDominantCategoryOnlyWhenAnotherCategoryHasStrictlyMoreReports() {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(
                new PhoneNumber("+37255512346")
        );
        FraudCategory police = categoryRepository.findByCode("POLICE").orElseThrow();
        FraudCategory bank = categoryRepository.findByCode("BANK_EMPLOYEE").orElseThrow();
        User firstBankReporter = saveUser("dominant-bank-first@example.test");
        User secondBankReporter = saveUser("dominant-bank-second@example.test");
        reportRepository.saveAndFlush(report(firstBankReporter, phoneNumber, bank));
        reportRepository.saveAndFlush(report(secondBankReporter, phoneNumber, bank));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());

        User firstPoliceReporter = saveUser("dominant-police-first@example.test");
        reportRepository.saveAndFlush(report(firstPoliceReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        var warning = warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        ).orElseThrow();
        assertThat(warning.getDominantCategory().getCode()).isEqualTo("BANK_EMPLOYEE");

        User secondPoliceReporter = saveUser("dominant-police-second@example.test");
        reportRepository.saveAndFlush(report(secondPoliceReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        assertThat(warning.getDominantCategory().getCode()).isEqualTo("BANK_EMPLOYEE");

        User thirdPoliceReporter = saveUser("dominant-police-third@example.test");
        reportRepository.saveAndFlush(report(thirdPoliceReporter, phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());
        assertThat(warning.getDominantCategory().getCode()).isEqualTo("POLICE");
    }

    @Test
    void shorteningExpirationPeriodImmediatelyExpiresInactiveWarnings() {
        PhoneNumber phoneNumber = phoneNumberRepository.saveAndFlush(
                new PhoneNumber("+37255512350")
        );
        FraudCategory police = categoryRepository.findByCode("POLICE").orElseThrow();
        reportRepository.saveAndFlush(report(saveUser("expiry-first@example.test"), phoneNumber, police));
        reportRepository.saveAndFlush(report(saveUser("expiry-second@example.test"), phoneNumber, police));
        warningService.recalculateForPhoneNumber(phoneNumber.getId());

        jdbcTemplate.update(
                "UPDATE report SET created_at = ? WHERE phone_number_id = ?",
                Timestamp.from(Instant.now().minusSeconds(3 * 24 * 60 * 60L)),
                phoneNumber.getId()
        );

        configurationService.updateExpirationPeriod(1);

        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.EXPIRED
        )).isPresent();
        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.ACTIVE
        )).isEmpty();

        configurationService.updateExpirationPeriod(3650);
        assertThat(warningRepository.findFirstByPhoneNumberIdAndStatus(
                phoneNumber.getId(),
                WarningStatus.EXPIRED
        )).isPresent();
    }

    private User saveUser(String email) {
        return userRepository.saveAndFlush(new User(email, "hash", UserRole.USER));
    }

    private Report report(User reporter, PhoneNumber phoneNumber, FraudCategory category) {
        return new Report(
                reporter,
                phoneNumber,
                category,
                LocalDate.now().minusDays(1),
                null,
                CommunicationChannel.PHONE_CALL,
                null
        );
    }
}
