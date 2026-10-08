package com.rentwise.training.dto;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;
import java.time.Instant;

public record AnswerFactDto(Long answerId, Long caseId, RiskTopic topic, Difficulty difficulty,
                            boolean correct, Instant answeredAt) {}
