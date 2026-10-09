package com.rentwise.training.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.security.CurrentUser;
import com.rentwise.training.dto.*;
import com.rentwise.training.service.DiagnosisWorkflowService;
import com.rentwise.training.service.TrainingFacade;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {
    private final TrainingFacade trainingFacade;
    private final DiagnosisWorkflowService workflowService;

    public DiagnosisController(TrainingFacade trainingFacade, DiagnosisWorkflowService workflowService) {
        this.trainingFacade = trainingFacade;
        this.workflowService = workflowService;
    }

    @PostMapping("/start")
    public ApiResponse<DiagnosisStartResponse> start(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(trainingFacade.startDiagnosis(currentUser.id()));
    }

    @PostMapping("/{sessionId}/answers")
    public ApiResponse<AnswerResult> answer(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ApiResponse.ok(trainingFacade.submitDiagnosisAnswer(
                currentUser.id(),
                sessionId,
                request.caseId(),
                request.selectedClarify()));
    }

    @PostMapping("/{sessionId}/finish")
    public ApiResponse<DiagnosisCompleteResponse> finish(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long sessionId) {
        return ApiResponse.ok(workflowService.finish(currentUser.id(), sessionId));
    }
}
