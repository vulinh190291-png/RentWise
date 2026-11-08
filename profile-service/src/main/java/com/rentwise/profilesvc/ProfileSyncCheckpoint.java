package com.rentwise.profilesvc;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_sync_checkpoint")
public class ProfileSyncCheckpoint {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_answer_id", nullable = false)
    private long lastAnswerId;

    // JPA 自动维护的并发控制版本。
    // 注意它和答题快照版本不是同一个值。
    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;

    protected ProfileSyncCheckpoint() {
    }

    public ProfileSyncCheckpoint(Long userId) {
        this.userId = userId;
        this.lastAnswerId = 0L;
    }

    public long getLastAnswerId() {
        return lastAnswerId;
    }

    public void advanceTo(long answerId) {
        if (answerId <= lastAnswerId) {
            throw new IllegalArgumentException(
                    "Snapshot version must increase"
            );
        }

        this.lastAnswerId = answerId;
    }
}
