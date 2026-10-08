package com.rentwise.training.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AssessmentFinishRequest(@NotEmpty List<@Valid AssessmentAnswerInput> answers) {}
