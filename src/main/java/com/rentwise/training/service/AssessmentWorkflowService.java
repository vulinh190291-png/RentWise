package com.rentwise.training.service;

import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Service
public class AssessmentWorkflowService {
    private final TrainingFacade trainingFacade;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;

    public AssessmentWorkflowService(TrainingFacade trainingFacade, ProfileFacade profileFacade, PlanFacade planFacade) {
        this.trainingFacade = trainingFacade;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
    }

    public DiagnosisStartResponse start(Long userId) {
        profileFacade.getProfile(userId); // fail clearly if diagnosis/profile has not been completed
        return trainingFacade.startAssessment(userId);
    }

    @Transactional
    public AssessmentResultResponse finish(Long sessionId, AssessmentFinishRequest request) {
        var topics = new LinkedHashSet<com.rentwise.training.domain.RiskTopic>();
        request.answers().forEach(input -> {
            var answer = trainingFacade.submitAssessmentAnswer(sessionId, input.caseId(), input.selectedClarify());
            topics.add(answer.topic());
        });
        var finish = trainingFacade.finishAssessment(sessionId);
        topics.forEach(topic -> profileFacade.recalculate(finish.userId(), topic));
        var profile = profileFacade.getProfile(finish.userId());
        planFacade.initializePlan(finish.userId());
        var next = planFacade.nextTraining(finish.userId());
        return new AssessmentResultResponse(sessionId, finish.userId(), profile, next);
    }
}
