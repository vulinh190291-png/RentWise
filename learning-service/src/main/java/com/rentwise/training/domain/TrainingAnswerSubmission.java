
package com.rentwise.training.domain;

import com.rentwise.common.exception.DomainException;
import com.rentwise.training.dto.AnswerResult;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Entity
@Table(
        name = "training_answer_submission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_training_submission_user_request",
                columnNames = {"user_id", "request_id"}
        )
)
public class TrainingAnswerSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_id", nullable = false, length = 36)
    private String requestId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "selected_clarify", nullable = false)
    private boolean selectedClarify;

    @Column(name = "answer_id", nullable = false)
    private Long answerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskTopic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TrainingAnswerSubmission() {
    }

    public TrainingAnswerSubmission(
            Long userId,
            String requestId,
            boolean selectedClarify,
            AnswerResult answer
    ) {
        this.userId = userId;
        this.requestId = requestId;
        this.caseId = answer.caseId();
        this.selectedClarify = selectedClarify;
        this.answerId = answer.answerId();
        this.topic = answer.topic();
        this.difficulty = answer.difficulty();
        this.correct = answer.correct();
        this.createdAt = Instant.now();
    }

    public void verifyRequest(
            Long requestedCaseId,
            boolean requestedClarify
    ) {
        if (!caseId.equals(requestedCaseId)
                || selectedClarify != requestedClarify) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "Request ID was already used for another answer"
            );
        }
    }

    public AnswerResult toAnswerResult() {
        return new AnswerResult(
                answerId,
                caseId,
                topic,
                difficulty,
                correct
        );
    }
}
