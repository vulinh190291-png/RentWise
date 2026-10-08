package com.rentwise.profile.service;

import com.rentwise.common.exception.DomainException;
import com.rentwise.profile.domain.UserMastery;
import com.rentwise.profile.dto.*;
import com.rentwise.profile.repository.UserMasteryRepository;
import com.rentwise.training.domain.*;
import com.rentwise.training.dto.AnswerFactDto;
import com.rentwise.training.service.TrainingFacade;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ProfileService implements ProfileFacade {
    private static final int WINDOW_SIZE = 8;
    private final UserMasteryRepository masteryRepository;
    private final TrainingFacade trainingFacade;

    public ProfileService(UserMasteryRepository masteryRepository, TrainingFacade trainingFacade) {
        this.masteryRepository = masteryRepository;
        this.trainingFacade = trainingFacade;
    }

    @Override
    public UserProfileResponse initializeFromDiagnosis(Long userId) {
        for (RiskTopic topic : RiskTopic.values()) {
            masteryRepository.findByUserIdAndTopic(userId, topic)
                    .orElseGet(() -> masteryRepository.save(new UserMastery(userId, topic)));
            recalculate(userId, topic);
        }
        return getProfile(userId);
    }

    @Override
    public TopicMasteryView recalculate(Long userId, RiskTopic topic) {
        List<AnswerFactDto> recent = trainingFacade.recentAnswers(userId, topic, WINDOW_SIZE);
        if (recent.isEmpty()) {
            throw new DomainException(HttpStatus.CONFLICT, "No answer facts available for topic " + topic);
        }
        int totalWeight = recent.stream().mapToInt(a -> a.difficulty().weight()).sum();
        int correctWeight = recent.stream().filter(AnswerFactDto::correct).mapToInt(a -> a.difficulty().weight()).sum();
        int score = (int) Math.round(correctWeight * 100.0 / totalWeight);
        int consecutiveWrong = calculateConsecutiveWrong(userId, topic);
        Difficulty lastDifficulty = recent.get(0).difficulty();

        UserMastery mastery = masteryRepository.findByUserIdAndTopic(userId, topic)
                .orElseGet(() -> new UserMastery(userId, topic));
        mastery.update(score, consecutiveWrong, lastDifficulty);
        return toView(masteryRepository.save(mastery));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        List<UserMastery> rows = masteryRepository.findByUserId(userId);
        if (rows.size() != RiskTopic.values().length) {
            throw new DomainException(HttpStatus.NOT_FOUND, "Profile not initialized for user " + userId);
        }
        Map<RiskTopic, UserMastery> byTopic = new EnumMap<>(RiskTopic.class);
        rows.forEach(row -> byTopic.put(row.getTopic(), row));
        List<TopicMasteryView> topics = Arrays.stream(RiskTopic.values()).map(t -> toView(byTopic.get(t))).toList();
        return new UserProfileResponse(userId, topics);
    }

    private int calculateConsecutiveWrong(Long userId, RiskTopic topic) {
        int count = 0;
        for (AnswerFactDto fact : trainingFacade.recentAnswersForUser(userId, WINDOW_SIZE)) {
            if (fact.topic() != topic || fact.correct()) {
                break;
            }
            count++;
        }
        return count;
    }

    private TopicMasteryView toView(UserMastery mastery) {
        return new TopicMasteryView(mastery.getTopic(), mastery.getTopic().displayName(), mastery.getMasteryScore(),
                level(mastery.getMasteryScore()), mastery.getConsecutiveWrong(), mastery.getLastDifficulty());
    }

    private String level(int score) {
        if (score < 50) return "WEAK";
        if (score < 80) return "NORMAL";
        return "MASTERED";
    }
}
