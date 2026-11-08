package com.rentwise.training.service;

import com.rentwise.profile.sync.ProfileSyncQueue;
import com.rentwise.training.domain.AnswerSource;
import com.rentwise.training.dto.AnswerResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingAnswerPersistenceService {

    private final TrainingFacade trainingFacade;
    private final ProfileSyncQueue syncQueue;

    public TrainingAnswerPersistenceService(
            TrainingFacade trainingFacade,
            ProfileSyncQueue syncQueue
    ) {
        this.trainingFacade = trainingFacade;
        this.syncQueue = syncQueue;
    }

    @Transactional
    public AnswerResult saveAnswer(
            Long userId,
            Long caseId,
            boolean selectedClarify
    ) {
        // 在同一个本地事务中保存答题记录
        AnswerResult answer = trainingFacade.recordAnswer(
                userId,
                null,
                caseId,
                selectedClarify,
                AnswerSource.TRAINING
        );

        // 同一事务中插入待同步任务
        syncQueue.enqueue(userId);

        return answer;
    }
}
