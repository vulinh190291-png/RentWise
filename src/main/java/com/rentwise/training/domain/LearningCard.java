package com.rentwise.training.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "learning_card", uniqueConstraints = @UniqueConstraint(columnNames = "topic"))
public class LearningCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RiskTopic topic;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1500)
    private String content;

    protected LearningCard() {}
    public LearningCard(RiskTopic topic, String title, String content) {
        this.topic = topic; this.title = title; this.content = content;
    }
    public Long getId() { return id; }
    public RiskTopic getTopic() { return topic; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}
