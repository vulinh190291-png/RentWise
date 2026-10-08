package com.rentwise.training.dto;

import com.rentwise.training.domain.Difficulty;
import com.rentwise.training.domain.RiskTopic;

public record CaseView(Long caseId, RiskTopic topic, Difficulty difficulty, String clauseText, String question) {}
