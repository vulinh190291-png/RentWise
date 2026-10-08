package com.rentwise.training.service;

import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.domain.AnswerSource;
import com.rentwise.training.dto.TrainingAnswerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingWorkflowService {
    private final TrainingFacade trainingFacade;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;

    public TrainingWorkflowService(TrainingFacade trainingFacade, ProfileFacade profileFacade, PlanFacade planFacade) {
        this.trainingFacade = trainingFacade;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
    }

    @Transactional
    public TrainingAnswerResponse submit(Long userId, Long caseId, boolean selectedClarify) {
        var answer = trainingFacade.recordAnswer(userId, null, caseId, selectedClarify, AnswerSource.TRAINING);
        var feedback = trainingFacade.feedback(caseId, answer.correct());
        var mastery = profileFacade.recalculate(userId, answer.topic());
        var next = planFacade.nextTraining(userId);
        return new TrainingAnswerResponse(answer, feedback, mastery, next);
    }
}
