package com.rentwise.training.dto;

import jakarta.validation.constraints.NotNull;

public record TrainingAnswerRequest(@NotNull Long userId, @NotNull Long caseId, boolean selectedClarify) {}
