package com.rentwise.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentwise.auth.dto.RegisterRequest;
import com.rentwise.auth.service.AuthService;
import com.rentwise.user.domain.UserAccount;
import com.rentwise.user.domain.UserRole;
import com.rentwise.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityIntegrationTest.AdminTestController.class)
class SecurityIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired AuthService authService;
    @Autowired JwtService jwtService;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void protectedRouteWithoutTokenReturnsJson401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void malformedAndTamperedBearerTokensReturn401() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));

        UserAccount user = userRepository.save(new UserAccount(
                "Atopos", passwordEncoder.encode("12345678"), UserRole.LEARNER, true));
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void validTokenPopulatesCurrentUser() throws Exception {
        UserAccount user = userRepository.save(new UserAccount(
                "Atopos", passwordEncoder.encode("12345678"), UserRole.LEARNER, true));
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value("Atopos"))
                .andExpect(jsonPath("$.data.role").value("LEARNER"));
    }

    @Test
    void badAndUnknownLoginReturnSameGeneric401() throws Exception {
        authService.register(new RegisterRequest("Atopos", "12345678"));
        String badPassword = "{\"username\":\"Atopos\",\"password\":\"wrongpass\"}";
        String unknown = "{\"username\":\"Nobody\",\"password\":\"12345678\"}";

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(badPassword))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(unknown))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void nonBearerAuthorizationHeaderDoesNotAuthenticate() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void unlistedRoutesRequireAuthenticationByDefault() throws Exception {
        mockMvc.perform(get("/test/authenticated-only"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void methodSecurityReturns403ForLearnerAndAllowsAdmin() throws Exception {
        UserAccount learner = userRepository.save(new UserAccount(
                "learner", passwordEncoder.encode("12345678"), UserRole.LEARNER, true));
        UserAccount admin = userRepository.save(new UserAccount(
                "admin", passwordEncoder.encode("12345678"), UserRole.ADMIN, true));

        mockMvc.perform(get("/test/admin").header("Authorization", "Bearer " + jwtService.generateToken(learner)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        mockMvc.perform(get("/test/admin").header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-ok"));
    }

    @Test
    void openApiPublishesBearerAuthScheme() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);

        JsonNode scheme = root.path("components").path("securitySchemes").path("bearerAuth");
        assertThat(scheme.path("type").asText()).isEqualTo("http");
        assertThat(scheme.path("scheme").asText()).isEqualTo("bearer");
        assertThat(scheme.path("bearerFormat").asText()).isEqualTo("JWT");
    }

    @RestController
    static class AdminTestController {
        @GetMapping("/test/authenticated-only")
        String authenticatedOnly() {
            return "authenticated-ok";
        }

        @GetMapping("/test/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "admin-ok";
        }
    }
}
