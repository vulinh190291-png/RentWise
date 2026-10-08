package com.rentwise.training.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "diagnosis_session")
public class DiagnosisSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private SessionType sessionType;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private SessionStatus status;
    @Column(nullable = false)
    private Instant startedAt;
    private Instant finishedAt;

    protected DiagnosisSession() {}

    public DiagnosisSession(Long userId, SessionType sessionType) {
        this.userId = userId;
        this.sessionType = sessionType;
        this.status = SessionStatus.STARTED;
        this.startedAt = Instant.now();
    }

    public void finish() {
        if (status != SessionStatus.FINISHED) {
            status = SessionStatus.FINISHED;
            finishedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public SessionType getSessionType() { return sessionType; }
    public SessionStatus getStatus() { return status; }
}
