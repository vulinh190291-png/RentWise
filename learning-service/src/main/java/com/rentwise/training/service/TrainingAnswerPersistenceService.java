
package com.rentwise.training.service;

import com.rentwise.profile.sync.ProfileSyncQueue;
import com.rentwise.training.domain.AnswerSource;
import com.rentwise.training.domain.TrainingAnswerSubmission;
import com.rentwise.training.dto.AnswerResult;
import com.rentwise.training.repository.TrainingAnswerSubmissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TrainingAnswerPersistenceService {

    private final TrainingFacade trainingFacade;
    private final ProfileSyncQueue syncQueue;
    private final TrainingAnswerSubmissionRepository repository;

    public TrainingAnswerPersistenceService(
            TrainingFacade trainingFacade,
            ProfileSyncQueue syncQueue,
            TrainingAnswerSubmissionRepository repository
    ) {
        this.trainingFacade = trainingFacade;
        this.syncQueue = syncQueue;
        this.repository = repository;
    }

    @Transactional
    public SavedTrainingAnswer saveAnswer(
            Long userId,
            Long caseId,
            boolean selectedClarify,
            UUID requestId
    ) {
        // 同一个 requestId 已提交过：返回原答案
        Optional<AnswerResult> existing =
                findExistingAnswerInternal(
                        userId,
                        caseId,
                        selectedClarify,
                        requestId
                );

        if (existing.isPresent()) {
            return new SavedTrainingAnswer(
                    existing.get(),
                    null
            );
        }

        // 保存真实答题记录
        AnswerResult answer = trainingFacade.recordAnswer(
                userId,
                null,
                caseId,
                selectedClarify,
                AnswerSource.TRAINING
        );

        // 创建 Outbox 待同步任务
        Long syncJobId = syncQueue.enqueue(userId);

        // 保存请求编号与真实答案的对应关系
        TrainingAnswerSubmission submission =
                new TrainingAnswerSubmission(
                        userId,
                        requestId.toString(),
                        selectedClarify,
                        answer
                );

        repository.saveAndFlush(submission);

        return new SavedTrainingAnswer(
                answer,
                syncJobId
        );
    }

    @Transactional(readOnly = true)
    public Optional<AnswerResult> findExistingAnswer(
            Long userId,
            Long caseId,
            boolean selectedClarify,
            UUID requestId
    ) {
        return findExistingAnswerInternal(
                userId,
                caseId,
                selectedClarify,
                requestId
        );
    }

    private Optional<AnswerResult> findExistingAnswerInternal(
            Long userId,
            Long caseId,
            boolean selectedClarify,
            UUID requestId
    ) {
        return repository
                .findByUserIdAndRequestId(
                        userId,
                        requestId.toString()
                )
                .map(submission -> {
                    submission.verifyRequest(
                            caseId,
                            selectedClarify
                    );
                    return submission.toAnswerResult();
                });
    }

    public record SavedTrainingAnswer(
            AnswerResult answer,
            Long syncJobId
    ) {
    }
}
