package com.example.javaproject;

import com.example.javaproject.repository.FraudCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class JavaProjectApplicationTests extends PostgresIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FraudCategoryRepository fraudCategoryRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void migrationSeedsInitialActiveFraudCategories() {
        assertThat(fraudCategoryRepository.findAllByActiveTrueOrderByNameAsc())
                .extracting("code")
                .containsExactly("BANK_EMPLOYEE", "POLICE", "POSTAL_WORKER");
    }

    @Test
    void openApiDocumentationIsPublicAndDescribesProtectedApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Scam Report API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/v1/reports'].post.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/categories'].get.security").doesNotExist());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsLocalFrontendCorsPreflightRequests() throws Exception {
        mockMvc.perform(options("/api/v1/reports")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    void rejectsCorsRequestsFromUnconfiguredOrigins() throws Exception {
        mockMvc.perform(options("/api/v1/reports")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
