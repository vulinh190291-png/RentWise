package com.rentwise.training.service;

import com.rentwise.training.domain.*;
import com.rentwise.training.dto.*;
import java.util.List;
import java.util.Optional;

public interface TrainingFacade {
    DiagnosisStartResponse startDiagnosis(Long userId);
    AnswerResult submitDiagnosisAnswer(Long sessionId, Long caseId, boolean selectedClarify);
    DiagnosisFinishResult finishDiagnosis(Long sessionId);
    List<AnswerFactDto> recentAnswers(Long userId, RiskTopic topic, int limit);
    List<AnswerFactDto> recentAnswersForUser(Long userId, int limit);
    Optional<AnswerFactDto> latestAnswer(Long userId);
    CaseView selectCase(RiskTopic topic, Difficulty preferredDifficulty, Long excludeCaseId);
    Optional<LearningCardView> learningCard(RiskTopic topic);
    FeedbackView feedback(Long caseId, boolean correct);
    AnswerResult recordAnswer(Long userId, Long sessionId, Long caseId, boolean selectedClarify, AnswerSource source);
    DiagnosisStartResponse startAssessment(Long userId);
    AnswerResult submitAssessmentAnswer(Long sessionId, Long caseId, boolean selectedClarify);
    DiagnosisFinishResult finishAssessment(Long sessionId);
}
