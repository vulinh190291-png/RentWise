
package com.rentwise.training.service;

import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.dto.TrainingAnswerResponse;
import org.springframework.stereotype.Service;

@Service
public class TrainingWorkflowService {

    private final TrainingFacade trainingFacade;
    private final TrainingAnswerPersistenceService persistenceService;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;

    public TrainingWorkflowService(
            TrainingFacade trainingFacade,
            TrainingAnswerPersistenceService persistenceService,
            ProfileFacade profileFacade,
            PlanFacade planFacade
    ) {
        this.trainingFacade = trainingFacade;
        this.persistenceService = persistenceService;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
    }

    public TrainingAnswerResponse submit(
            Long userId,
            Long caseId,
            boolean selectedClarify
    ) {
        // 答案与待同步任务一起提交
        var answer = persistenceService.saveAnswer(
                userId,
                caseId,
                selectedClarify
        );

        var feedback = trainingFacade.feedback(
                caseId,
                answer.correct()
        );

        // 正常情况下仍立即同步 Profile
        var mastery = profileFacade.recalculate(
                userId,
                answer.topic()
        );

        var next = planFacade.nextTraining(userId);

        return new TrainingAnswerResponse(
                answer,
                feedback,
                mastery,
                next
        );
    }
}
