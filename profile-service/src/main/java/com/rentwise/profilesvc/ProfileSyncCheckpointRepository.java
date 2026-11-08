package com.rentwise.profilesvc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileSyncCheckpointRepository
        extends JpaRepository<ProfileSyncCheckpoint, Long> {
}
