package com.rentwise.profile.domain;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_mastery", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic"}))
public class UserMastery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RiskTopic topic;
    @Column(name = "mastery_score", nullable = false)
    private int masteryScore;
    @Column(name = "consecutive_wrong", nullable = false)
    private int consecutiveWrong;
    @Enumerated(EnumType.STRING) @Column(name = "last_difficulty")
    private Difficulty lastDifficulty;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserMastery() {}
    public UserMastery(Long userId, RiskTopic topic) {
        this.userId = userId;
        this.topic = topic;
        this.masteryScore = 0;
        this.consecutiveWrong = 0;
        this.updatedAt = Instant.now();
    }

    public void update(int score, int consecutiveWrong, Difficulty lastDifficulty) {
        this.masteryScore = score;
        this.consecutiveWrong = consecutiveWrong;
        this.lastDifficulty = lastDifficulty;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public RiskTopic getTopic() { return topic; }
    public int getMasteryScore() { return masteryScore; }
    public int getConsecutiveWrong() { return consecutiveWrong; }
    public Difficulty getLastDifficulty() { return lastDifficulty; }
}
