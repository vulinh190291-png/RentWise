package com.rentwise.training.dto;

import com.rentwise.plan.dto.NextTrainingResponse;
import com.rentwise.profile.dto.UserProfileResponse;

public record AssessmentResultResponse(Long sessionId, Long userId, UserProfileResponse profile,
                                       NextTrainingResponse nextTraining) {}
