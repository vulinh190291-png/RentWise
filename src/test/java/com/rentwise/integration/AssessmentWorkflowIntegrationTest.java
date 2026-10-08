package com.rentwise.integration;

import com.rentwise.training.dto.AssessmentAnswerInput;
import com.rentwise.training.dto.AssessmentFinishRequest;
import com.rentwise.training.service.AssessmentWorkflowService;
import com.rentwise.training.service.DiagnosisWorkflowService;
import com.rentwise.training.service.TrainingFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssessmentWorkflowIntegrationTest {
    @Autowired TrainingFacade trainingFacade;
    @Autowired DiagnosisWorkflowService diagnosisWorkflowService;
    @Autowired AssessmentWorkflowService assessmentWorkflowService;

    @Test
    void mixedTopicAssessmentPersistsAnswersUpdatesMasteryAndReturnsNextStageRecommendation() {
        Long userId = 601L;
        var diagnosis = trainingFacade.startDiagnosis(userId);
        diagnosis.cases().forEach(c -> trainingFacade.submitDiagnosisAnswer(diagnosis.sessionId(), c.caseId(), false));
        var before = diagnosisWorkflowService.finish(diagnosis.sessionId()).profile();

        var assessment = assessmentWorkflowService.start(userId);
        assertThat(assessment.cases()).extracting(c -> c.topic()).doesNotHaveDuplicates();
        assertThat(assessment.cases()).hasSize(5);

        var request = new AssessmentFinishRequest(assessment.cases().stream()
                .map(c -> new AssessmentAnswerInput(c.caseId(), true))
                .toList());
        var result = assessmentWorkflowService.finish(assessment.sessionId(), request);

        assertThat(result.profile().topics()).hasSize(5);
        assertThat(result.profile().topics()).isNotEqualTo(before.topics());
        assertThat(result.nextTraining()).isNotNull();
        assertThat(trainingFacade.recentAnswersForUser(userId, 5)).hasSize(5);
    }
}
