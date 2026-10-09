package com.rentwise.training.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.security.CurrentUser;
import com.rentwise.training.dto.TrainingAnswerRequest;
import com.rentwise.training.dto.TrainingAnswerResponse;
import com.rentwise.training.service.TrainingWorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training")
public class TrainingController {
    private final TrainingWorkflowService workflowService;

    public TrainingController(TrainingWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/answers")
    public ApiResponse<TrainingAnswerResponse> answer(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody TrainingAnswerRequest request) {
        return ApiResponse.ok(workflowService.submit(
                currentUser.id(),
                request.caseId(),
                request.selectedClarify()));
    }
}
