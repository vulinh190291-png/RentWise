package com.rentwise.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentwise.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthenticatedLearningFlowIntegrationTest.AdminTestController.class)
class AuthenticatedLearningFlowIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void authenticatedLearnerCompletesFullRentWiseFlowWithoutClientUserId() throws Exception {
        String token = registerAndLogin("flowuser", "12345678");

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("flowuser"))
                .andExpect(jsonPath("$.data.role").value("LEARNER"));

        JsonNode diagnosis = startDiagnosis(token);
        long diagnosisSessionId = diagnosis.path("sessionId").asLong();
        answerAllDiagnosisCases(token, diagnosis);

        mockMvc.perform(post("/api/diagnosis/{sessionId}/finish", diagnosisSessionId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.topics.length()").value(5));

        mockMvc.perform(get("/api/profile/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topics.length()").value(5));

        JsonNode nextTraining = readData(mockMvc.perform(get("/api/training/next")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        long nextCaseId = nextTraining.path("trainingCase").path("caseId").asLong();

        mockMvc.perform(post("/api/training/answers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseId\":" + nextCaseId + ",\"selectedClarify\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedMastery").exists())
                .andExpect(jsonPath("$.data.nextTraining").exists());

        JsonNode assessment = readData(mockMvc.perform(post("/api/assessment/start")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        long assessmentSessionId = assessment.path("sessionId").asLong();
        mockMvc.perform(post("/api/assessment/{sessionId}/finish", assessmentSessionId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assessmentFinishBody(assessment.path("cases"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.topics.length()").value(5))
                .andExpect(jsonPath("$.data.nextTraining").exists());

        mockMvc.perform(get("/api/profile/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topics.length()").value(5));
        mockMvc.perform(get("/api/training/next").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));

        mockMvc.perform(get("/test/e2e-admin").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        long otherUserId = userRepository.save(
                new com.rentwise.user.domain.UserAccount(
                        "other", "$2a$10$hash", com.rentwise.user.domain.UserRole.LEARNER, true)).getId();
        mockMvc.perform(get("/api/profile/{userId}", otherUserId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private JsonNode startDiagnosis(String token) throws Exception {
        String json = mockMvc.perform(post("/api/diagnosis/start")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return readData(json);
    }

    private void answerAllDiagnosisCases(String token, JsonNode diagnosis) throws Exception {
        long sessionId = diagnosis.path("sessionId").asLong();
        for (JsonNode trainingCase : diagnosis.path("cases")) {
            long caseId = trainingCase.path("caseId").asLong();
            boolean selected = trainingCase.path("difficulty").asText().equals("EASY");
            mockMvc.perform(post("/api/diagnosis/{sessionId}/answers", sessionId)
                            .header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"caseId\":" + caseId + ",\"selectedClarify\":" + selected + "}"))
                    .andExpect(status().isOk());
        }
    }

    private String assessmentFinishBody(JsonNode cases) {
        StringBuilder body = new StringBuilder("{\"answers\":[");
        for (int i = 0; i < cases.size(); i++) {
            if (i > 0) body.append(',');
            body.append("{\"caseId\":")
                    .append(cases.get(i).path("caseId").asLong())
                    .append(",\"selectedClarify\":true}");
        }
        return body.append("]}").toString();
    }

    private String registerAndLogin(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return readData(loginJson).path("accessToken").asText();
    }

    private JsonNode readData(String json) throws Exception {
        return objectMapper.readTree(json).path("data");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @RestController
    static class AdminTestController {
        @GetMapping("/test/e2e-admin")
        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "admin-ok";
        }
    }
}
