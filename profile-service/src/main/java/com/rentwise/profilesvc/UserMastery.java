
package com.rentwise.profilesvc;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "user_mastery",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "topic"}
        )
)
public class UserMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String topic;

    @Column(name = "mastery_score", nullable = false)
    private int masteryScore;

    @Column(name = "consecutive_wrong", nullable = false)
    private int consecutiveWrong;

    @Column(name = "last_difficulty", length = 32)
    private String lastDifficulty;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserMastery() {
    }

    public UserMastery(Long userId, String topic) {
        this.userId = userId;
        this.topic = topic;
        this.updatedAt = Instant.now();
    }

    public void update(
            int score,
            int consecutiveWrong,
            String lastDifficulty
    ) {
        this.masteryScore = score;
        this.consecutiveWrong = consecutiveWrong;
        this.lastDifficulty = lastDifficulty;
        this.updatedAt = Instant.now();
    }

    public Long getUserId() {
        return userId;
    }

    public String getTopic() {
        return topic;
    }

    public int getMasteryScore() {
        return masteryScore;
    }

    public int getConsecutiveWrong() {
        return consecutiveWrong;
    }

    public String getLastDifficulty() {
        return lastDifficulty;
    }
}