package com.rentwise.profile.sync;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "profile_sync_outbox",
        indexes = @Index(
                name = "idx_profile_sync_due",
                columnList = "status,next_attempt_at"
        )
)
public class ProfileSyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "last_error", length = 256)
    private String lastError;

    protected ProfileSyncJob() {
    }

    public ProfileSyncJob(Long userId) {
        this.userId = userId;
        this.status = "PENDING";
        this.attempts = 0;
        this.createdAt = Instant.now();
        this.nextAttemptAt = Instant.now();
    }

    public void markDone() {
        this.status = "DONE";
        this.finishedAt = Instant.now();
        this.lastError = null;
    }

    public void scheduleRetry(Exception exception) {
        this.status = "PENDING";
        this.attempts++;

        long delaySeconds = Math.min(
                300L,
                10L * (1L << Math.min(attempts, 4))
        );

        this.nextAttemptAt =
                Instant.now().plusSeconds(delaySeconds);

        // 只记录异常类型，不保存密钥等敏感信息
        this.lastError =
                exception.getClass().getSimpleName();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }
}