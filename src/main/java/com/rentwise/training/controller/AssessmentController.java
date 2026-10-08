package com.rentwise.training.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.training.dto.*;
import com.rentwise.training.service.AssessmentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {
    private final AssessmentWorkflowService workflowService;
    public AssessmentController(AssessmentWorkflowService workflowService) { this.workflowService = workflowService; }

    @PostMapping("/start")
    public ApiResponse<DiagnosisStartResponse> start(@RequestParam Long userId) {
        return ApiResponse.ok(workflowService.start(userId));
    }

    @PostMapping("/{sessionId}/finish")
    public ApiResponse<AssessmentResultResponse> finish(@PathVariable Long sessionId,
                                                         @Valid @RequestBody AssessmentFinishRequest request) {
        return ApiResponse.ok(workflowService.finish(sessionId, request));
    }
}
