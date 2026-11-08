
package com.rentwise.profile.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface ProfileSyncJobRepository
        extends JpaRepository<ProfileSyncJob, Long> {

    List<ProfileSyncJob>
    findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            String status,
            Instant nextAttemptAt
    );

    @Modifying
    @Transactional
    @Query("""
            update ProfileSyncJob j
            set j.status = 'DONE',
                j.finishedAt = :finishedAt,
                j.lastError = null
            where j.id = :jobId
              and j.status = 'PENDING'
            """)
    int markDone(
            @Param("jobId") Long jobId,
            @Param("finishedAt") Instant finishedAt
    );
}
