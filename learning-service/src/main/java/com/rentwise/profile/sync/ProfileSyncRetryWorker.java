
package com.rentwise.profile.sync;

import com.rentwise.profile.service.ProfileFacade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "rentwise.profile",
        name = "remote-enabled",
        havingValue = "true"
)
public class ProfileSyncRetryWorker {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProfileSyncRetryWorker.class
            );

    private final ProfileSyncJobRepository repository;
    private final ProfileFacade profileFacade;

    public ProfileSyncRetryWorker(
            ProfileSyncJobRepository repository,
            ProfileFacade profileFacade
    ) {
        this.repository = repository;
        this.profileFacade = profileFacade;
    }

    @Scheduled(
            fixedDelayString =
                    "${rentwise.profile.retry-delay-ms:10000}",
            initialDelayString =
                    "${rentwise.profile.retry-initial-ms:10000}"
    )
    public void retryPending() {

        List<ProfileSyncJob> jobs = repository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        "PENDING",
                        Instant.now()
                );

        for (ProfileSyncJob job : jobs) {
            try {
                // 每次重试都读取 Training 最新的已提交答案
                profileFacade.initializeFromDiagnosis(
                        job.getUserId()
                );

                job.markDone();
                repository.save(job);

                log.info(
                        "Profile synchronization completed, jobId={}",
                        job.getId()
                );

            } catch (Exception ex) {
                job.scheduleRetry(ex);
                repository.save(job);

                log.warn(
                        "Profile synchronization failed, jobId={}, attempts={}",
                        job.getId(),
                        job.getAttempts()
                );
            }
        }
    }
}
