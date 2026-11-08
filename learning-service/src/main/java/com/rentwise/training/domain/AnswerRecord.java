package com.rentwise.training.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "answer_record", indexes = {
        @Index(name = "idx_answer_user_topic_time", columnList = "user_id,topic,answered_at"),
        @Index(name = "idx_answer_session", columnList = "session_id")
})
public class AnswerRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "session_id")
    private Long sessionId;
    @Column(name = "case_id", nullable = false)
    private Long caseId;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RiskTopic topic;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Difficulty difficulty;
    @Column(nullable = false)
    private boolean correct;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AnswerSource source;
    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    protected AnswerRecord() {}

    public AnswerRecord(Long userId, Long sessionId, Long caseId, RiskTopic topic, Difficulty difficulty,
                        boolean correct, AnswerSource source) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.caseId = caseId;
        this.topic = topic;
        this.difficulty = difficulty;
        this.correct = correct;
        this.source = source;
        this.answeredAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getSessionId() { return sessionId; }
    public Long getCaseId() { return caseId; }
    public RiskTopic getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public boolean isCorrect() { return correct; }
    public AnswerSource getSource() { return source; }
    public Instant getAnsweredAt() { return answeredAt; }
}
