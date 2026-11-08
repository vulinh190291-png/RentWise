
package com.rentwise.training.service;

import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.dto.DiagnosisCompleteResponse;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisWorkflowService {

    private final DiagnosisPersistenceService persistenceService;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;

    public DiagnosisWorkflowService(
            DiagnosisPersistenceService persistenceService,
            ProfileFacade profileFacade,
            PlanFacade planFacade
    ) {
        this.persistenceService = persistenceService;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
    }

    public DiagnosisCompleteResponse finish(
            Long userId,
            Long sessionId
    ) {
        // 第一步：提交诊断会话和 Outbox 任务
        var finish = persistenceService.finishAndEnqueue(
                userId,
                sessionId
        );

        // 第二步：同步到独立 Profile Service
        var profile = profileFacade.initializeFromDiagnosis(
                finish.userId()
        );

        // 第三步：根据最新学习画像生成训练计划
        var plan = planFacade.initializePlan(
                finish.userId()
        );

        var next = planFacade.nextTraining(
                finish.userId()
        );

        return new DiagnosisCompleteResponse(
                sessionId,
                finish.userId(),
                profile,
                plan,
                next
        );
    }
}
