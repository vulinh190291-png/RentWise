package com.rentwise.plan.dto;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;

public record PlanSummary(Long userId, RiskTopic focusTopic, Difficulty preferredDifficulty) {}
