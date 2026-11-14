package com.rentwise.profile;

import com.rentwise.common.exception.DomainException;
import com.rentwise.common.exception.GlobalExceptionHandler;
import com.rentwise.profile.controller.ProfileController;
import com.rentwise.profile.service.RemoteProfileFacade;
import com.rentwise.security.CurrentUser;
import com.rentwise.training.service.TrainingSnapshotService;
import com.rentwise.user.domain.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RemoteProfileFacadeTest {
    private MockRestServiceServer server;
    private RemoteProfileFacade facade;
    private TrainingSnapshotService snapshots;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("http://profile.test");
        server = MockRestServiceServer.bindTo(builder).build();
        snapshots = mock(TrainingSnapshotService.class);
        facade = new RemoteProfileFacade(snapshots, "http://profile.test", "test-only-key");
        ReflectionTestUtils.setField(facade, "profileClient", builder.build());
        mvc = MockMvcBuilders.standaloneSetup(new ProfileController(facade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
                new CurrentUser(42L, "profile-contract-test", UserRole.LEARNER), null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        server.verify();
    }

    private void respond(HttpMethod method, String path, HttpStatus status, String body) {
        server.expect(requestTo("http://profile.test" + path))
                .andExpect(method(method))
                .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON).body(body));
    }

    private void respondToGet(HttpStatus status, String body) {
        respond(HttpMethod.GET, "/internal/profiles/42", status, body);
    }

    private String initializedProfile() {
        return "{\"userId\":42,\"topics\":[" + String.join(",", List.of(
                "DEPOSIT_RETURN", "EARLY_TERMINATION", "REPAIR_RESPONSIBILITY", "COST_BEARING", "BREACH_LIABILITY")
                .stream().map(topic -> "{\"topic\":\"" + topic + "\",\"masteryScore\":60}").toList()) + "]}";
    }

    @Test
    void missingProfilePreservesNotFound() {
        respondToGet(HttpStatus.NOT_FOUND, "{\"status\":404}");
        assertThatThrownBy(() -> facade.getProfile(42L)).isInstanceOfSatisfying(DomainException.class, ex -> {
            assertThat(ex.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("Profile not initialized for user 42");
        });
        verifyNoInteractions(snapshots);
    }

    @Test
    void upstreamFaultsRemainBadGateway() {
        for (int code : new int[]{401, 403, 409, 500, 503}) {
            server.reset();
            respondToGet(HttpStatus.valueOf(code), "{}");
            assertThatThrownBy(() -> facade.getProfile(42L)).isInstanceOfSatisfying(DomainException.class,
                    ex -> assertThat(ex.status()).isEqualTo(HttpStatus.BAD_GATEWAY));
            server.verify();
        }
    }

    @Test
    void invalidSuccessBodyRemainsBadGateway() {
        respondToGet(HttpStatus.OK, "{\"userId\":42,\"topics\":[]}");
        assertThatThrownBy(() -> facade.getProfile(42L)).isInstanceOfSatisfying(DomainException.class,
                ex -> assertThat(ex.status()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void validProfileReturnedUnchanged() {
        respondToGet(HttpStatus.OK, initializedProfile());
        var profile = facade.getProfile(42L);
        assertThat(profile.userId()).isEqualTo(42L);
        assertThat(profile.topics()).hasSize(5).allSatisfy(topic -> assertThat(topic.masteryScore()).isEqualTo(60));
        verifyNoInteractions(snapshots);
    }

    @Test
    void syncNotFoundIsStillBadGateway() {
        when(snapshots.build(42L)).thenReturn(new TrainingSnapshotService.ProfileSnapshot(1L, List.of(), List.of()));
        respond(HttpMethod.POST, "/internal/profiles/42/sync", HttpStatus.NOT_FOUND, "{}");
        assertThatThrownBy(() -> facade.initializeFromDiagnosis(42L)).isInstanceOfSatisfying(DomainException.class,
                ex -> assertThat(ex.status()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void unreachableServiceRemainsBadGateway() {
        server.expect(requestTo("http://profile.test/internal/profiles/42"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> { throw new IOException("connection refused"); });
        assertThatThrownBy(() -> facade.getProfile(42L)).isInstanceOfSatisfying(DomainException.class,
                ex -> assertThat(ex.status()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void missingProfileHttpContractHasNoFabricatedData() throws Exception {
        respondToGet(HttpStatus.NOT_FOUND, "{\"status\":404}");
        mvc.perform(get("/api/profile/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Profile not initialized for user 42"));
        verifyNoInteractions(snapshots);
    }

    @Test
    void initializedProfileHttpContractStaysSuccessful() throws Exception {
        respondToGet(HttpStatus.OK, initializedProfile());
        mvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(42))
                .andExpect(jsonPath("$.data.topics.length()").value(5))
                .andExpect(jsonPath("$.data.topics[0].masteryScore").value(60));
        verifyNoInteractions(snapshots);
    }
}
