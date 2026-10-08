package com.rentwise.training.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "training_case")
public class TrainingCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RiskTopic topic;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Difficulty difficulty;
    @Column(name = "clause_text", nullable = false, length = 1200)
    private String clauseText;
    @Column(nullable = false, length = 500)
    private String question;
    @Column(name = "should_clarify", nullable = false)
    private boolean shouldClarify;
    @Column(name = "diagnosis_eligible", nullable = false)
    private boolean diagnosisEligible;
    @Column(nullable = false, length = 1500)
    private String explanation;
    @Column(name = "follow_up_question", nullable = false, length = 800)
    private String followUpQuestion;
    @Column(nullable = false)
    private boolean active = true;

    protected TrainingCase() {}

    public TrainingCase(RiskTopic topic, Difficulty difficulty, String clauseText, String question,
                        boolean shouldClarify, boolean diagnosisEligible) {
        this(topic, difficulty, clauseText, question, shouldClarify, diagnosisEligible,
                "请结合条款是否明确限定责任范围、条件和处理方式进行判断。",
                "如果实际签约，你会继续确认哪些条件和责任边界？");
    }

    public TrainingCase(RiskTopic topic, Difficulty difficulty, String clauseText, String question,
                        boolean shouldClarify, boolean diagnosisEligible, String explanation, String followUpQuestion) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.clauseText = clauseText;
        this.question = question;
        this.shouldClarify = shouldClarify;
        this.diagnosisEligible = diagnosisEligible;
        this.explanation = explanation;
        this.followUpQuestion = followUpQuestion;
        this.active = true;
    }

    public Long getId() { return id; }
    public RiskTopic getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getClauseText() { return clauseText; }
    public String getQuestion() { return question; }
    public boolean isShouldClarify() { return shouldClarify; }
    public boolean isDiagnosisEligible() { return diagnosisEligible; }
    public String getExplanation() { return explanation; }
    public String getFollowUpQuestion() { return followUpQuestion; }
    public boolean isActive() { return active; }
}
