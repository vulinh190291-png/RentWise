
package com.rentwise.training.service;

import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.dto.AssessmentFinishRequest;
import com.rentwise.training.dto.AssessmentResultResponse;
import com.rentwise.training.dto.DiagnosisStartResponse;
import org.springframework.stereotype.Service;

@Service
public class AssessmentWorkflowService {

    private final TrainingFacade trainingFacade;
    private final AssessmentPersistenceService persistenceService;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;

    public AssessmentWorkflowService(
            TrainingFacade trainingFacade,
            AssessmentPersistenceService persistenceService,
            ProfileFacade profileFacade,
            PlanFacade planFacade
    ) {
        this.trainingFacade = trainingFacade;
        this.persistenceService = persistenceService;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
    }

    public DiagnosisStartResponse start(Long userId) {
        profileFacade.getProfile(userId);
        return trainingFacade.startAssessment(userId);
    }

    public AssessmentResultResponse finish(
            Long userId,
            Long sessionId,
            AssessmentFinishRequest request
    ) {
        // 第一步：一次性提交五道答案和测评会话
        var finish = persistenceService.saveAndFinish(
                userId,
                sessionId,
                request
        );

        // 第二步：本地事务结束后，刷新五主题画像
        var profile = profileFacade.initializeFromDiagnosis(
                finish.userId()
        );

        // 第三步：重新生成训练计划
        planFacade.initializePlan(finish.userId());

        var next = planFacade.nextTraining(
                finish.userId()
        );

        return new AssessmentResultResponse(
                sessionId,
                finish.userId(),
                profile,
                next
        );
    }
}
