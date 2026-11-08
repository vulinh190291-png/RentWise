package com.rentwise.training.dto;

import com.rentwise.plan.dto.NextTrainingResponse;
import com.rentwise.plan.dto.PlanSummary;
import com.rentwise.profile.dto.UserProfileResponse;

public record DiagnosisCompleteResponse(Long sessionId, Long userId, UserProfileResponse profile,
                                        PlanSummary plan, NextTrainingResponse nextTraining) {}
