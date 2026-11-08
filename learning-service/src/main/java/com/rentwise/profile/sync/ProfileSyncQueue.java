package com.rentwise.profile.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileSyncQueue {

    private final ProfileSyncJobRepository repository;
    private final boolean remoteEnabled;

    public ProfileSyncQueue(
            ProfileSyncJobRepository repository,
            @Value("${rentwise.profile.remote-enabled:false}")
            boolean remoteEnabled
    ) {
        this.repository = repository;
        this.remoteEnabled = remoteEnabled;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(Long userId) {
        if (remoteEnabled) {
            repository.save(new ProfileSyncJob(userId));
        }
    }
}
