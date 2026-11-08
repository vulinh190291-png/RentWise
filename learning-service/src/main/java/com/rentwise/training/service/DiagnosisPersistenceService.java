
package com.rentwise.training.service;

import com.rentwise.profile.sync.ProfileSyncQueue;
import com.rentwise.training.dto.DiagnosisFinishResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagnosisPersistenceService {

    private final TrainingFacade trainingFacade;
    private final ProfileSyncQueue syncQueue;

    public DiagnosisPersistenceService(
            TrainingFacade trainingFacade,
            ProfileSyncQueue syncQueue
    ) {
        this.trainingFacade = trainingFacade;
        this.syncQueue = syncQueue;
    }

    @Transactional
    public DiagnosisFinishResult finishAndEnqueue(
            Long userId,
            Long sessionId
    ) {
        // 1. 检查十题诊断并完成会话
        DiagnosisFinishResult finish =
                trainingFacade.finishDiagnosis(
                        userId,
                        sessionId
                );

        // 2. 第一次完成时，创建画像同步任务
        // 两个数据库操作处于同一笔本地事务
        if (!finish.alreadyFinished()) {
            syncQueue.enqueue(finish.userId());
        }

        return finish;
    }
}
