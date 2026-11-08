package com.rentwise.plan.service;

import com.rentwise.plan.domain.TrainingPlan;
import com.rentwise.plan.dto.*;
import com.rentwise.plan.repository.TrainingPlanRepository;
import com.rentwise.profile.dto.TopicMasteryView;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.domain.*;
import com.rentwise.training.dto.LearningCardView;
import com.rentwise.training.service.TrainingFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@Transactional
public class PlanService implements PlanFacade {
    private final ProfileFacade profileFacade;
    private final TrainingFacade trainingFacade;
    private final TrainingPlanRepository planRepository;

    public PlanService(ProfileFacade profileFacade, TrainingFacade trainingFacade, TrainingPlanRepository planRepository) {
        this.profileFacade = profileFacade;
        this.trainingFacade = trainingFacade;
        this.planRepository = planRepository;
    }

    @Override
    public PlanSummary initializePlan(Long userId) {
        TopicMasteryView weakest = weakest(userId);
        Difficulty difficulty = targetDifficulty(weakest);
        TrainingPlan plan = planRepository.findByUserId(userId)
                .orElseGet(() -> new TrainingPlan(userId, weakest.topic(), difficulty));
        plan.update(weakest.topic(), difficulty);
        planRepository.save(plan);
        return new PlanSummary(userId, weakest.topic(), difficulty);
    }

    @Override
    public NextTrainingResponse nextTraining(Long userId) {
        TopicMasteryView weakest = weakest(userId);
        Difficulty difficulty = targetDifficulty(weakest);
        Long excludeCaseId = trainingFacade.latestAnswer(userId).map(a -> a.caseId()).orElse(null);
        var trainingCase = trainingFacade.selectCase(weakest.topic(), difficulty, excludeCaseId);
        LearningCardView card = weakest.consecutiveWrong() >= 2
                ? trainingFacade.learningCard(weakest.topic()).orElse(null) : null;

        TrainingPlan plan = planRepository.findByUserId(userId)
                .orElseGet(() -> new TrainingPlan(userId, weakest.topic(), difficulty));
        plan.update(weakest.topic(), difficulty);
        planRepository.save(plan);

        String reason = weakest.consecutiveWrong() >= 2
                ? "同主题连续答错，先复习识别卡并降低一档难度"
                : "优先训练当前 Mastery 最低主题";
        return new NextTrainingResponse(userId, weakest.topic(), difficulty, trainingCase, card, reason);
    }

    private TopicMasteryView weakest(Long userId) {
        return profileFacade.getProfile(userId).topics().stream()
                .min(Comparator.comparingInt(TopicMasteryView::masteryScore)
                        .thenComparingInt(v -> v.topic().ordinal()))
                .orElseThrow();
    }

    private Difficulty targetDifficulty(TopicMasteryView mastery) {
        if (mastery.consecutiveWrong() >= 2 && mastery.lastDifficulty() != null) {
            return mastery.lastDifficulty().downgrade();
        }
        if (mastery.masteryScore() < 50) return Difficulty.EASY;
        if (mastery.masteryScore() < 80) return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }
}
