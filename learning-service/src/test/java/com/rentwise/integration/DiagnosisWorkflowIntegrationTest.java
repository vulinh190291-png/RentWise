package com.rentwise.integration;

import com.rentwise.plan.repository.TrainingPlanRepository;
import com.rentwise.profile.repository.UserMasteryRepository;
import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;
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
class DiagnosisWorkflowIntegrationTest {
    @Autowired TrainingFacade trainingFacade;
    @Autowired DiagnosisWorkflowService diagnosisWorkflowService;
    @Autowired UserMasteryRepository masteryRepository;
    @Autowired TrainingPlanRepository planRepository;

    @Test
    void completesDiagnosisBuildsFiveTopicProfileAndRecommendsWeakestTopic() {
        Long userId = 501L;
        var started = trainingFacade.startDiagnosis(userId);
        started.cases().forEach(c -> {
            boolean correctChoice = c.difficulty() == Difficulty.EASY;
            boolean selected = c.topic() == RiskTopic.REPAIR_RESPONSIBILITY ? !correctChoice : correctChoice;
            trainingFacade.submitDiagnosisAnswer(userId, started.sessionId(), c.caseId(), selected);
        });

        var completed = diagnosisWorkflowService.finish(userId, started.sessionId());

        assertThat(completed.profile().topics()).hasSize(5);
        assertThat(completed.nextTraining().topic()).isEqualTo(RiskTopic.REPAIR_RESPONSIBILITY);
    }

    @Test
    void finishingSameDiagnosisTwiceDoesNotDuplicateMasteryOrPlan() {
        Long userId = 502L;
        var started = trainingFacade.startDiagnosis(userId);
        started.cases().forEach(c -> trainingFacade.submitDiagnosisAnswer(userId, started.sessionId(), c.caseId(), c.difficulty() == Difficulty.EASY));

        diagnosisWorkflowService.finish(userId, started.sessionId());
        diagnosisWorkflowService.finish(userId, started.sessionId());

        assertThat(masteryRepository.countByUserId(userId)).isEqualTo(5);
        assertThat(planRepository.countByUserId(userId)).isEqualTo(1);
    }
}
