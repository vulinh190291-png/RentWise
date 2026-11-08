package com.rentwise.plan;

import com.rentwise.plan.domain.TrainingPlan;
import com.rentwise.plan.repository.TrainingPlanRepository;
import com.rentwise.plan.service.PlanService;
import com.rentwise.profile.dto.*;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.domain.*;
import com.rentwise.training.dto.*;
import com.rentwise.training.service.TrainingFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlanServiceTest {
    ProfileFacade profileFacade;
    TrainingFacade trainingFacade;
    TrainingPlanRepository planRepository;
    PlanService planService;

    @BeforeEach
    void setUp() {
        profileFacade = mock(ProfileFacade.class);
        trainingFacade = mock(TrainingFacade.class);
        planRepository = mock(TrainingPlanRepository.class);
        when(planRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(planRepository.save(any(TrainingPlan.class))).thenAnswer(inv -> inv.getArgument(0));
        planService = new PlanService(profileFacade, trainingFacade, planRepository);
    }

    @Test
    void selectsWeakestTopicAndBreaksTiesByEnumOrder() {
        Long userId = 1L;
        when(profileFacade.getProfile(userId)).thenReturn(profile(userId,
                view(RiskTopic.DEPOSIT_RETURN, 40, 0, Difficulty.MEDIUM),
                view(RiskTopic.EARLY_TERMINATION, 40, 0, Difficulty.MEDIUM),
                view(RiskTopic.REPAIR_RESPONSIBILITY, 70, 0, Difficulty.MEDIUM),
                view(RiskTopic.COST_BEARING, 80, 0, Difficulty.MEDIUM),
                view(RiskTopic.BREACH_LIABILITY, 90, 0, Difficulty.MEDIUM)));
        when(trainingFacade.latestAnswer(userId)).thenReturn(Optional.empty());
        when(trainingFacade.selectCase(eq(RiskTopic.DEPOSIT_RETURN), any(), isNull()))
                .thenReturn(new CaseView(11L, RiskTopic.DEPOSIT_RETURN, Difficulty.EASY, "clause", "question"));
        when(trainingFacade.learningCard(any())).thenReturn(Optional.empty());

        var next = planService.nextTraining(userId);
        assertThat(next.topic()).isEqualTo(RiskTopic.DEPOSIT_RETURN);
    }

    @Test
    void downgradesDifficultyOneLevelAfterTwoConsecutiveWrongAnswers() {
        Long userId = 2L;
        when(profileFacade.getProfile(userId)).thenReturn(profile(userId,
                view(RiskTopic.DEPOSIT_RETURN, 20, 2, Difficulty.HARD),
                view(RiskTopic.EARLY_TERMINATION, 60, 0, Difficulty.MEDIUM),
                view(RiskTopic.REPAIR_RESPONSIBILITY, 60, 0, Difficulty.MEDIUM),
                view(RiskTopic.COST_BEARING, 60, 0, Difficulty.MEDIUM),
                view(RiskTopic.BREACH_LIABILITY, 60, 0, Difficulty.MEDIUM)));
        when(trainingFacade.latestAnswer(userId)).thenReturn(Optional.empty());
        when(trainingFacade.selectCase(eq(RiskTopic.DEPOSIT_RETURN), eq(Difficulty.MEDIUM), isNull()))
                .thenReturn(new CaseView(12L, RiskTopic.DEPOSIT_RETURN, Difficulty.MEDIUM, "clause", "question"));
        when(trainingFacade.learningCard(RiskTopic.DEPOSIT_RETURN))
                .thenReturn(Optional.of(new LearningCardView(RiskTopic.DEPOSIT_RETURN, "card", "content")));

        var next = planService.nextTraining(userId);
        assertThat(next.difficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(next.learningCard()).isNotNull();
        assertThat(Difficulty.MEDIUM.downgrade()).isEqualTo(Difficulty.EASY);
        assertThat(Difficulty.EASY.downgrade()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void asksTrainingToAvoidImmediatelyRepeatingLastCase() {
        Long userId = 3L;
        when(profileFacade.getProfile(userId)).thenReturn(profile(userId,
                view(RiskTopic.REPAIR_RESPONSIBILITY, 20, 0, Difficulty.EASY),
                view(RiskTopic.DEPOSIT_RETURN, 80, 0, Difficulty.EASY),
                view(RiskTopic.EARLY_TERMINATION, 80, 0, Difficulty.EASY),
                view(RiskTopic.COST_BEARING, 80, 0, Difficulty.EASY),
                view(RiskTopic.BREACH_LIABILITY, 80, 0, Difficulty.EASY)));
        when(trainingFacade.latestAnswer(userId)).thenReturn(Optional.of(
                new AnswerFactDto(1L, 41L, RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, false, Instant.now())));
        when(trainingFacade.selectCase(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, 41L))
                .thenReturn(new CaseView(42L, RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, "other", "question"));
        when(trainingFacade.learningCard(any())).thenReturn(Optional.empty());

        var next = planService.nextTraining(userId);
        assertThat(next.trainingCase().caseId()).isEqualTo(42L);
        verify(trainingFacade).selectCase(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, 41L);
    }

    private UserProfileResponse profile(Long userId, TopicMasteryView... views) {
        return new UserProfileResponse(userId, List.of(views));
    }

    private TopicMasteryView view(RiskTopic topic, int score, int wrong, Difficulty last) {
        return new TopicMasteryView(topic, topic.displayName(), score, score < 50 ? "WEAK" : score < 80 ? "NORMAL" : "MASTERED", wrong, last);
    }
}
