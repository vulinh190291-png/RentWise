package com.rentwise.training.service;

import com.rentwise.common.exception.DomainException;
import com.rentwise.plan.service.PlanFacade;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.dto.AnswerResult;
import com.rentwise.training.dto.TrainingAnswerResponse;
import com.rentwise.profile.sync.ProfileSyncJobRepository;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TrainingWorkflowService {

    private final TrainingFacade trainingFacade;
    private final TrainingAnswerPersistenceService persistenceService;
    private final ProfileFacade profileFacade;
    private final PlanFacade planFacade;
    private final ProfileSyncJobRepository syncJobRepository;


    public TrainingWorkflowService(
            TrainingFacade trainingFacade,
            TrainingAnswerPersistenceService persistenceService,
            ProfileFacade profileFacade,
            PlanFacade planFacade,
            ProfileSyncJobRepository syncJobRepository
    ) {
        this.trainingFacade = trainingFacade;
        this.persistenceService = persistenceService;
        this.profileFacade = profileFacade;
        this.planFacade = planFacade;
        this.syncJobRepository = syncJobRepository;
    }



    public TrainingAnswerResponse submit(
            Long userId,
            Long caseId,
            boolean selectedClarify,
            UUID requestId
    ) {
        TrainingAnswerPersistenceService.SavedTrainingAnswer saved;

        try {
            saved = persistenceService.saveAnswer(
                    userId,
                    caseId,
                    selectedClarify,
                    requestId
            );

        } catch (DataIntegrityViolationException ex) {

            // 并发重复请求：读取已经成功保存的答案
            var existing = persistenceService.findExistingAnswer(
                    userId,
                    caseId,
                    selectedClarify,
                    requestId
            ).orElseThrow(() -> ex);

            saved = new TrainingAnswerPersistenceService.SavedTrainingAnswer(
                    existing,
                    null
            );
        }

        var answer = saved.answer();

        var feedback = trainingFacade.feedback(
                caseId,
                answer.correct()
        );

        try {
            var mastery = profileFacade.recalculate(
                    userId,
                    answer.topic()
            );

            // Profile 已成功同步，立即确认当前 Outbox 任务
            if (saved.syncJobId() != null) {
                syncJobRepository.markDone(
                        saved.syncJobId(),
                        Instant.now()
                );
            }

            var next = planFacade.nextTraining(userId);

            return new TrainingAnswerResponse(
                    answer,
                    feedback,
                    mastery,
                    next,
                    "SYNCED"
            );

        } catch (DomainException ex) {

            if (ex.status() != HttpStatus.BAD_GATEWAY
                    && ex.status() != HttpStatus.GATEWAY_TIMEOUT) {
                throw ex;
            }

            return new TrainingAnswerResponse(
                    answer,
                    feedback,
                    null,
                    null,
                    "PENDING"
            );
        }
    }

}
