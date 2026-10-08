package com.rentwise.training.dto;

import jakarta.validation.constraints.NotNull;

public record AssessmentAnswerInput(@NotNull Long caseId, boolean selectedClarify) {}
