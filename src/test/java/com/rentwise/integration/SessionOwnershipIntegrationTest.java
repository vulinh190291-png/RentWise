package com.rentwise.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentwise.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionOwnershipIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void anotherUserCannotAnswerOrFinishDiagnosisOrAssessmentSession() throws Exception {
        String tokenA = registerAndLogin("ownerA", "12345678");
        String tokenB = registerAndLogin("ownerB", "12345678");

        JsonNode diagnosis = startDiagnosis(tokenA);
        long diagnosisSessionId = diagnosis.path("sessionId").asLong();
        long firstCaseId = diagnosis.path("cases").get(0).path("caseId").asLong();

        mockMvc.perform(post("/api/diagnosis/{sessionId}/answers", diagnosisSessionId)
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseId\":" + firstCaseId + ",\"selectedClarify\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Diagnosis session not found"));

        mockMvc.perform(post("/api/diagnosis/{sessionId}/finish", diagnosisSessionId)
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Diagnosis session not found"));

        answerAllDiagnosisCases(tokenA, diagnosis);
        mockMvc.perform(post("/api/diagnosis/{sessionId}/finish", diagnosisSessionId)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());

        String assessmentJson = mockMvc.perform(post("/api/assessment/start")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode assessment = objectMapper.readTree(assessmentJson).path("data");
        long assessmentSessionId = assessment.path("sessionId").asLong();
        String assessmentBody = assessmentFinishBody(assessment.path("cases"));

        mockMvc.perform(post("/api/assessment/{sessionId}/finish", assessmentSessionId)
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assessmentBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Assessment session not found"));
    }

    private JsonNode startDiagnosis(String token) throws Exception {
        String json = mockMvc.perform(post("/api/diagnosis/start")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data");
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
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(login).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
