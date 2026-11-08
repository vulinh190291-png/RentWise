package com.rentwise.profile.dto;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;

public record TopicMasteryView(RiskTopic topic, String topicName, int masteryScore,
                               String level, int consecutiveWrong, Difficulty lastDifficulty) {}
