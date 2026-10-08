package com.rentwise.training.service;

import com.rentwise.common.exception.DomainException;
import com.rentwise.training.domain.*;
import com.rentwise.training.dto.*;
import com.rentwise.training.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TrainingService implements TrainingFacade {
    private final TrainingCaseRepository caseRepository;
    private final DiagnosisSessionRepository sessionRepository;
    private final AnswerRecordRepository answerRepository;
    private final LearningCardRepository learningCardRepository;

    public TrainingService(TrainingCaseRepository caseRepository, DiagnosisSessionRepository sessionRepository,
                           AnswerRecordRepository answerRepository, LearningCardRepository learningCardRepository) {
        this.caseRepository = caseRepository;
        this.sessionRepository = sessionRepository;
        this.answerRepository = answerRepository;
        this.learningCardRepository = learningCardRepository;
    }

    @Override
    public DiagnosisStartResponse startDiagnosis(Long userId) {
        var cases = caseRepository.findByDiagnosisEligibleTrueAndActiveTrueOrderByIdAsc();
        if (cases.size() != 10) {
            throw new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, "Demo diagnosis requires exactly 10 cases");
        }
        DiagnosisSession session = sessionRepository.save(new DiagnosisSession(userId, SessionType.DIAGNOSIS));
        return new DiagnosisStartResponse(session.getId(), userId, cases.stream().map(this::toCaseView).toList());
    }

    @Override
    public AnswerResult submitDiagnosisAnswer(Long sessionId, Long caseId, boolean selectedClarify) {
        DiagnosisSession session = findOpenSession(sessionId, SessionType.DIAGNOSIS);
        TrainingCase trainingCase = findCase(caseId);
        if (!trainingCase.isDiagnosisEligible()) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Case is not part of diagnosis");
        }
        if (answerRepository.existsBySessionIdAndCaseId(sessionId, caseId)) {
            throw new DomainException(HttpStatus.CONFLICT, "Case already answered in this session");
        }
        return saveAnswer(session.getUserId(), sessionId, trainingCase, selectedClarify, AnswerSource.DIAGNOSIS);
    }

    @Override
    public DiagnosisFinishResult finishDiagnosis(Long sessionId) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "Diagnosis session not found"));
        if (session.getSessionType() != SessionType.DIAGNOSIS) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Not a diagnosis session");
        }
        boolean alreadyFinished = session.getStatus() == SessionStatus.FINISHED;
        if (!alreadyFinished) {
            long answers = answerRepository.countBySessionId(sessionId);
            if (answers != 10) {
                throw new DomainException(HttpStatus.BAD_REQUEST, "Diagnosis requires 10 answered cases before finish");
            }
            session.finish();
            sessionRepository.save(session);
        }
        return new DiagnosisFinishResult(session.getId(), session.getUserId(), alreadyFinished);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerFactDto> recentAnswers(Long userId, RiskTopic topic, int limit) {
        return answerRepository.findByUserIdAndTopicOrderByAnsweredAtDescIdDesc(userId, topic, PageRequest.of(0, limit))
                .stream().map(this::toFact).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerFactDto> recentAnswersForUser(Long userId, int limit) {
        return answerRepository.findByUserIdOrderByAnsweredAtDescIdDesc(userId, PageRequest.of(0, limit))
                .stream().map(this::toFact).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnswerFactDto> latestAnswer(Long userId) {
        return recentAnswersForUser(userId, 1).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public CaseView selectCase(RiskTopic topic, Difficulty preferredDifficulty, Long excludeCaseId) {
        List<TrainingCase> preferred = caseRepository.findByTopicAndDifficultyAndActiveTrueOrderByIdAsc(topic, preferredDifficulty);
        TrainingCase selected = firstNotExcluded(preferred, excludeCaseId)
                .orElseGet(() -> firstNotExcluded(caseRepository.findByTopicAndActiveTrueOrderByIdAsc(topic), excludeCaseId)
                        .orElseGet(() -> caseRepository.findByTopicAndActiveTrueOrderByIdAsc(topic).stream().findFirst()
                                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "No active case for topic"))));
        return toCaseView(selected);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LearningCardView> learningCard(RiskTopic topic) {
        return learningCardRepository.findByTopic(topic)
                .map(card -> new LearningCardView(card.getTopic(), card.getTitle(), card.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackView feedback(Long caseId, boolean correct) {
        TrainingCase trainingCase = findCase(caseId);
        return new FeedbackView(correct, trainingCase.getExplanation(), trainingCase.getFollowUpQuestion());
    }

    @Override
    public AnswerResult recordAnswer(Long userId, Long sessionId, Long caseId, boolean selectedClarify, AnswerSource source) {
        TrainingCase trainingCase = findCase(caseId);
        if (sessionId != null && answerRepository.existsBySessionIdAndCaseId(sessionId, caseId)) {
            throw new DomainException(HttpStatus.CONFLICT, "Case already answered in this session");
        }
        return saveAnswer(userId, sessionId, trainingCase, selectedClarify, source);
    }

    @Override
    public DiagnosisStartResponse startAssessment(Long userId) {
        DiagnosisSession session = sessionRepository.save(new DiagnosisSession(userId, SessionType.ASSESSMENT));
        List<CaseView> cases = Arrays.stream(RiskTopic.values())
                .map(topic -> caseRepository.findByTopicAndActiveTrueOrderByIdAsc(topic).stream().findFirst()
                        .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "No assessment case for " + topic)))
                .map(this::toCaseView)
                .toList();
        return new DiagnosisStartResponse(session.getId(), userId, cases);
    }

    @Override
    public AnswerResult submitAssessmentAnswer(Long sessionId, Long caseId, boolean selectedClarify) {
        DiagnosisSession session = findOpenSession(sessionId, SessionType.ASSESSMENT);
        if (answerRepository.existsBySessionIdAndCaseId(sessionId, caseId)) {
            throw new DomainException(HttpStatus.CONFLICT, "Case already answered in this assessment");
        }
        return saveAnswer(session.getUserId(), sessionId, findCase(caseId), selectedClarify, AnswerSource.ASSESSMENT);
    }

    @Override
    public DiagnosisFinishResult finishAssessment(Long sessionId) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "Assessment session not found"));
        if (session.getSessionType() != SessionType.ASSESSMENT) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Not an assessment session");
        }
        boolean alreadyFinished = session.getStatus() == SessionStatus.FINISHED;
        if (!alreadyFinished) {
            long answers = answerRepository.countBySessionId(sessionId);
            if (answers != RiskTopic.values().length) {
                throw new DomainException(HttpStatus.BAD_REQUEST, "Assessment requires exactly 5 answered cases before finish");
            }
            session.finish();
            sessionRepository.save(session);
        }
        return new DiagnosisFinishResult(session.getId(), session.getUserId(), alreadyFinished);
    }

    private DiagnosisSession findOpenSession(Long sessionId, SessionType type) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "Session not found"));
        if (session.getSessionType() != type || session.getStatus() != SessionStatus.STARTED) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Session is not open");
        }
        return session;
    }

    private TrainingCase findCase(Long caseId) {
        return caseRepository.findById(caseId)
                .filter(TrainingCase::isActive)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "Training case not found"));
    }

    private AnswerResult saveAnswer(Long userId, Long sessionId, TrainingCase trainingCase,
                                    boolean selectedClarify, AnswerSource source) {
        boolean correct = selectedClarify == trainingCase.isShouldClarify();
        AnswerRecord saved = answerRepository.save(new AnswerRecord(userId, sessionId, trainingCase.getId(),
                trainingCase.getTopic(), trainingCase.getDifficulty(), correct, source));
        return new AnswerResult(saved.getId(), trainingCase.getId(), trainingCase.getTopic(), trainingCase.getDifficulty(), correct);
    }

    private Optional<TrainingCase> firstNotExcluded(List<TrainingCase> cases, Long excludeCaseId) {
        return cases.stream().filter(c -> excludeCaseId == null || !c.getId().equals(excludeCaseId)).findFirst();
    }

    private CaseView toCaseView(TrainingCase c) {
        return new CaseView(c.getId(), c.getTopic(), c.getDifficulty(), c.getClauseText(), c.getQuestion());
    }

    private AnswerFactDto toFact(AnswerRecord a) {
        return new AnswerFactDto(a.getId(), a.getCaseId(), a.getTopic(), a.getDifficulty(), a.isCorrect(), a.getAnsweredAt());
    }
}
