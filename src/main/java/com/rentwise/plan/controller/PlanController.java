package com.rentwise.plan.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.plan.dto.NextTrainingResponse;
import com.rentwise.plan.service.PlanFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training")
public class PlanController {
    private final PlanFacade planFacade;
    public PlanController(PlanFacade planFacade) { this.planFacade = planFacade; }

    @GetMapping("/next")
    public ApiResponse<NextTrainingResponse> next(@RequestParam Long userId) {
        return ApiResponse.ok(planFacade.nextTraining(userId));
    }
}
