package com.example.javaproject.category;

import com.example.javaproject.PostgresIntegrationTestBase;
import com.example.javaproject.entity.FraudCategory;
import com.example.javaproject.repository.FraudCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryAdminIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FraudCategoryRepository categoryRepository;

    @Test
    @WithMockUser(roles = "USER")
    void categoryManagementIsForbiddenForRegularUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanCreateDeactivateAndReactivateCategories() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "test_scammer",
                                  "name": "Test scammer",
                                  "warningTitle": "Scammer posing as a test scammer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST_SCAMMER"))
                .andExpect(jsonPath("$.active").value(true));

        FraudCategory category = categoryRepository.findByCode("TEST_SCAMMER").orElseThrow();

        mockMvc.perform(delete("/api/v1/admin/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        assertThat(categoryRepository.existsById(category.getId())).isTrue();
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().isActive()).isFalse();

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'TEST_SCAMMER')]").isEmpty());

        mockMvc.perform(put("/api/v1/admin/categories/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated test scammer",
                                  "warningTitle": "Updated warning title",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated test scammer"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'TEST_SCAMMER')]").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void categoryCodesAreUniqueRegardlessOfCase() throws Exception {
        categoryRepository.saveAndFlush(new FraudCategory("CUSTOM_ROLE", "Custom", "Custom warning"));

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "custom_role",
                                  "name": "Duplicate",
                                  "warningTitle": "Duplicate warning"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_CODE_ALREADY_EXISTS"));
    }
}
