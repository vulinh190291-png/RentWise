package com.rentwise.training.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.security.CurrentUser;
import com.rentwise.training.dto.AssessmentFinishRequest;
import com.rentwise.training.dto.AssessmentResultResponse;
import com.rentwise.training.dto.DiagnosisStartResponse;
import com.rentwise.training.service.AssessmentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {
    private final AssessmentWorkflowService workflowService;

    public AssessmentController(AssessmentWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/start")
    public ApiResponse<DiagnosisStartResponse> start(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(workflowService.start(currentUser.id()));
    }

    @PostMapping("/{sessionId}/finish")
    public ApiResponse<AssessmentResultResponse> finish(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long sessionId,
            @Valid @RequestBody AssessmentFinishRequest request) {
        return ApiResponse.ok(workflowService.finish(currentUser.id(), sessionId, request));
    }
}
