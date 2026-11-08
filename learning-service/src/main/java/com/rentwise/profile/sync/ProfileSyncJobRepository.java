package com.rentwise.profile.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ProfileSyncJobRepository
        extends JpaRepository<ProfileSyncJob, Long> {

    List<ProfileSyncJob>
    findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            String status,
            Instant nextAttemptAt
    );
}
