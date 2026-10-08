package com.rentwise.training;

import com.rentwise.training.dto.AnswerResult;
import com.rentwise.training.dto.DiagnosisStartResponse;
import com.rentwise.training.repository.AnswerRecordRepository;
import com.rentwise.training.repository.DiagnosisSessionRepository;
import com.rentwise.training.service.TrainingFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TrainingServiceTest {

    @Autowired TrainingFacade trainingFacade;
    @Autowired AnswerRecordRepository answerRecordRepository;
    @Autowired DiagnosisSessionRepository diagnosisSessionRepository;

    @BeforeEach
    void cleanFacts() {
        answerRecordRepository.deleteAll();
        diagnosisSessionRepository.deleteAll();
    }

    @Test
    void startsDiagnosisWithExactlyTenCasesAcrossFiveTopics() {
        DiagnosisStartResponse started = trainingFacade.startDiagnosis(1L);

        assertThat(started.cases()).hasSize(10);
        Map<?, Long> counts = started.cases().stream()
                .collect(Collectors.groupingBy(c -> c.topic(), Collectors.counting()));
        assertThat(counts).hasSize(5);
        assertThat(counts.values()).allMatch(count -> count == 2L);
    }

    @Test
    void storesAnswerFactsIncludingCorrectnessTopicAndDifficulty() {
        DiagnosisStartResponse started = trainingFacade.startDiagnosis(1L);
        var first = started.cases().get(0);

        AnswerResult result = trainingFacade.submitDiagnosisAnswer(
                started.sessionId(), first.caseId(), true);

        var facts = trainingFacade.recentAnswers(1L, first.topic(), 8);
        assertThat(facts).hasSize(1);
        assertThat(facts.get(0).caseId()).isEqualTo(first.caseId());
        assertThat(facts.get(0).topic()).isEqualTo(first.topic());
        assertThat(facts.get(0).difficulty()).isEqualTo(first.difficulty());
        assertThat(facts.get(0).correct()).isEqualTo(result.correct());
    }

    @Test
    void finishingDiagnosisTwiceIsIdempotent() {
        DiagnosisStartResponse started = trainingFacade.startDiagnosis(1L);
        started.cases().forEach(c -> trainingFacade.submitDiagnosisAnswer(started.sessionId(), c.caseId(), true));

        var firstFinish = trainingFacade.finishDiagnosis(started.sessionId());
        var secondFinish = trainingFacade.finishDiagnosis(started.sessionId());

        assertThat(firstFinish.sessionId()).isEqualTo(secondFinish.sessionId());
        assertThat(diagnosisSessionRepository.count()).isEqualTo(1);
        assertThat(answerRecordRepository.countBySessionId(started.sessionId())).isEqualTo(10);
    }
}
