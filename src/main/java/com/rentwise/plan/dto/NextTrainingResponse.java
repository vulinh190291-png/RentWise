package com.rentwise.plan.dto;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;
import com.rentwise.training.dto.CaseView;
import com.rentwise.training.dto.LearningCardView;

public record NextTrainingResponse(Long userId, RiskTopic topic, Difficulty difficulty,
                                   CaseView trainingCase, LearningCardView learningCard, String reason) {}
