package com.rentwise.training.dto;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;

public record AnswerResult(Long answerId, Long caseId, RiskTopic topic, Difficulty difficulty, boolean correct) {}
