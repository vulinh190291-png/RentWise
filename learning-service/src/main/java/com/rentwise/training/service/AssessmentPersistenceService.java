
package com.rentwise.training.service;

import com.rentwise.profile.sync.ProfileSyncQueue;
import com.rentwise.training.dto.AssessmentFinishRequest;
import com.rentwise.training.dto.DiagnosisFinishResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentPersistenceService {

    private final TrainingFacade trainingFacade;
    private final ProfileSyncQueue syncQueue;

    public AssessmentPersistenceService(
            TrainingFacade trainingFacade,
            ProfileSyncQueue syncQueue
    ) {
        this.trainingFacade = trainingFacade;
        this.syncQueue = syncQueue;
    }

    @Transactional
    public DiagnosisFinishResult saveAndFinish(
            Long userId,
            Long sessionId,
            AssessmentFinishRequest request
    ) {
        // 1. 在一个本地事务中保存测评答案
        request.answers().forEach(input ->
                trainingFacade.submitAssessmentAnswer(
                        userId,
                        sessionId,
                        input.caseId(),
                        input.selectedClarify()
                )
        );

        // 2. 验证测评答案并完成会话
        DiagnosisFinishResult finish =
                trainingFacade.finishAssessment(
                        userId,
                        sessionId
                );

        // 3. 同一事务内创建 Outbox 任务
        if (!finish.alreadyFinished()) {
            syncQueue.enqueue(finish.userId());
        }

        return finish;
    }
}
