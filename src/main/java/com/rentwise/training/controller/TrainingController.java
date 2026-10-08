package com.rentwise.training.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.training.dto.*;
import com.rentwise.training.service.TrainingWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training")
public class TrainingController {
    private final TrainingWorkflowService workflowService;
    public TrainingController(TrainingWorkflowService workflowService) { this.workflowService = workflowService; }

    @PostMapping("/answers")
    public ApiResponse<TrainingAnswerResponse> answer(@Valid @RequestBody TrainingAnswerRequest request) {
        return ApiResponse.ok(workflowService.submit(request.userId(), request.caseId(), request.selectedClarify()));
    }
}
