package com.rentwise.training.service;

import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.dto.DiagnosisCompleteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagnosisWorkflowService {
    private final TrainingFacade trainingFacade;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;

    public DiagnosisWorkflowService(TrainingFacade trainingFacade, ProfileFacade profileFacade, PlanFacade planFacade) {
        this.trainingFacade = trainingFacade;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
    }

    @Transactional
    public DiagnosisCompleteResponse finish(Long sessionId) {
        var finish = trainingFacade.finishDiagnosis(sessionId);
        var profile = profileFacade.initializeFromDiagnosis(finish.userId());
        var plan = planFacade.initializePlan(finish.userId());
        var next = planFacade.nextTraining(finish.userId());
        return new DiagnosisCompleteResponse(sessionId, finish.userId(), profile, plan, next);
    }
}
