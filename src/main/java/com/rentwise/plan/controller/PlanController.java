package com.rentwise.plan.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.plan.dto.NextTrainingResponse;
import com.rentwise.plan.service.PlanFacade;
import com.rentwise.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training")
public class PlanController {
    private final PlanFacade planFacade;

    public PlanController(PlanFacade planFacade) {
        this.planFacade = planFacade;
    }

    @GetMapping("/next")
    public ApiResponse<NextTrainingResponse> next(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(planFacade.nextTraining(currentUser.id()));
    }
}
