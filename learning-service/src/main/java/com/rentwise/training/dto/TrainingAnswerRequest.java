package com.rentwise.training.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TrainingAnswerRequest(
        @NotNull Long caseId,
        boolean selectedClarify,
        @NotNull UUID requestId
) {
}