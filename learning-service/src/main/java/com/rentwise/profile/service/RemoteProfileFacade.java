
package com.rentwise.profile.service;

import com.rentwise.common.exception.DomainException;
import com.rentwise.profile.dto.TopicMasteryView;
import com.rentwise.profile.dto.UserProfileResponse;
import com.rentwise.training.domain.RiskTopic;
import com.rentwise.training.service.TrainingSnapshotService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@ConditionalOnProperty(
        prefix = "rentwise.profile",
        name = "remote-enabled",
        havingValue = "true"
)
public class RemoteProfileFacade implements ProfileFacade {

    private final TrainingSnapshotService snapshotService;
    private final RestClient profileClient;

    public RemoteProfileFacade(
            TrainingSnapshotService snapshotService,
            @Value("${rentwise.profile.base-url}") String baseUrl,
            @Value("${rentwise.internal-key}") String internalKey
    ) {
        this.snapshotService = snapshotService;

        this.profileClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Key", internalKey)
                .build();
    }

    @Override
    public UserProfileResponse initializeFromDiagnosis(Long userId) {
        return synchronize(userId);
    }

    @Override
    public TopicMasteryView recalculate(
            Long userId,
            RiskTopic topic
    ) {
        UserProfileResponse profile = synchronize(userId);

        return profile.topics()
                .stream()
                .filter(item -> item.topic() == topic)
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        HttpStatus.BAD_GATEWAY,
                        "Profile response missing topic: " + topic
                ));
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        try {
            UserProfileResponse result = profileClient
                    .get()
                    .uri("/internal/profiles/{userId}", userId)
                    .retrieve()
                    // A missing profile is a business precondition, not an upstream outage.
                    // Keep this mapping scoped to GET; synchronization failures remain 502.
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new DomainException(
                                HttpStatus.NOT_FOUND,
                                "Profile not initialized for user " + userId
                        );
                    })
                    .body(UserProfileResponse.class);

            return requireBody(result);

        } catch (RestClientException ex) {
            throw remoteError(ex);
        }
    }

    private UserProfileResponse synchronize(Long userId) {

        // 从 Training 自己的数据库读取真实答题事实
        var snapshot = snapshotService.build(userId);

        try {
            // 将快照发给独立的 Profile Service
            UserProfileResponse result = profileClient
                    .post()
                    .uri("/internal/profiles/{userId}/sync", userId)
                    .body(snapshot)
                    .retrieve()
                    .body(UserProfileResponse.class);

            return requireBody(result);

        } catch (RestClientException ex) {
            throw remoteError(ex);
        }
    }

    private UserProfileResponse requireBody(
            UserProfileResponse response
    ) {
        if (response == null || response.topics() == null
                || response.topics().size() != 5) {
            throw new DomainException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid response from Profile Service"
            );
        }

        return response;
    }

    private DomainException remoteError(
            RestClientException ex
    ) {
        return new DomainException(
                HttpStatus.BAD_GATEWAY,
                "Profile Service request failed: "
                        + ex.getClass().getSimpleName()
        );
    }
}
