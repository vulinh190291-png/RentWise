package com.rentwise.plan.domain;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "training_plan", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class TrainingPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING) @Column(name = "focus_topic", nullable = false)
    private RiskTopic focusTopic;
    @Enumerated(EnumType.STRING) @Column(name = "preferred_difficulty", nullable = false)
    private Difficulty preferredDifficulty;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TrainingPlan() {}
    public TrainingPlan(Long userId, RiskTopic focusTopic, Difficulty preferredDifficulty) {
        this.userId = userId;
        update(focusTopic, preferredDifficulty);
    }
    public void update(RiskTopic focusTopic, Difficulty preferredDifficulty) {
        this.focusTopic = focusTopic;
        this.preferredDifficulty = preferredDifficulty;
        this.updatedAt = Instant.now();
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public RiskTopic getFocusTopic() { return focusTopic; }
    public Difficulty getPreferredDifficulty() { return preferredDifficulty; }
}
