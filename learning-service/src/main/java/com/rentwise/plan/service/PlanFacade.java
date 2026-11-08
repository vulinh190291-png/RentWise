package com.rentwise.plan.service;

import com.rentwise.plan.dto.*;

public interface PlanFacade {
    PlanSummary initializePlan(Long userId);
    NextTrainingResponse nextTraining(Long userId);
}
