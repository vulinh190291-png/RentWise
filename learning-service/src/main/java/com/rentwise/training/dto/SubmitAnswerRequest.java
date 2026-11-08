package com.rentwise.training.dto;

import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(@NotNull Long caseId, boolean selectedClarify) {}
