package com.rentwise.training.dto;

import com.rentwise.training.domain.RiskTopic;

public record LearningCardView(RiskTopic topic, String title, String content) {}
