package com.rentwise.profile;

import com.rentwise.common.exception.DomainException;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.training.domain.*;
import com.rentwise.training.repository.TrainingCaseRepository;
import com.rentwise.training.service.TrainingFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileServiceTest {
    @Autowired TrainingFacade trainingFacade;
    @Autowired ProfileFacade profileFacade;
    @Autowired TrainingCaseRepository trainingCaseRepository;

    @Test
    void computesWeightedMasteryFromAvailableRecordsWhenFewerThanEight() {
        Long userId = 101L;
        var easy = trainingFacade.selectCase(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, null);
        var medium = trainingFacade.selectCase(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.MEDIUM, null);
        var hardEntity = trainingCaseRepository.save(new TrainingCase(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.HARD,
                "测试困难条款", "是否值得进一步确认？", true, false));

        trainingFacade.recordAnswer(userId, null, easy.caseId(), true, AnswerSource.TRAINING);    // correct, weight 1
        trainingFacade.recordAnswer(userId, null, medium.caseId(), true, AnswerSource.TRAINING); // wrong, weight 2
        trainingFacade.recordAnswer(userId, null, hardEntity.getId(), true, AnswerSource.TRAINING); // correct, weight 3

        var mastery = profileFacade.recalculate(userId, RiskTopic.REPAIR_RESPONSIBILITY);
        assertThat(mastery.masteryScore()).isEqualTo(67);
    }

    @Test
    void usesOnlyMostRecentEightSameTopicAnswers() {
        Long userId = 102L;
        var easy = trainingFacade.selectCase(RiskTopic.DEPOSIT_RETURN, Difficulty.EASY, null);
        var medium = trainingFacade.selectCase(RiskTopic.DEPOSIT_RETURN, Difficulty.MEDIUM, null);
        trainingFacade.recordAnswer(userId, null, easy.caseId(), false, AnswerSource.TRAINING); // old wrong
        for (int i = 0; i < 8; i++) {
            trainingFacade.recordAnswer(userId, null, medium.caseId(), false, AnswerSource.TRAINING); // correct
        }

        var mastery = profileFacade.recalculate(userId, RiskTopic.DEPOSIT_RETURN);
        assertThat(mastery.masteryScore()).isEqualTo(100);
    }

    @Test
    void initializesFiveTopicsFromDiagnosisAndIsIdempotent() {
        Long userId = 103L;
        var diagnosis = trainingFacade.startDiagnosis(userId);
        diagnosis.cases().forEach(c -> trainingFacade.submitDiagnosisAnswer(diagnosis.sessionId(), c.caseId(), true));
        trainingFacade.finishDiagnosis(diagnosis.sessionId());

        profileFacade.initializeFromDiagnosis(userId);
        profileFacade.initializeFromDiagnosis(userId);

        assertThat(profileFacade.getProfile(userId).topics()).hasSize(5);
    }

    @Test
    void uninitializedUserGetsClearDomainError() {
        assertThatThrownBy(() -> profileFacade.getProfile(99999L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not initialized");
    }

    @Test
    void consecutiveWrongCountIsScopedToUninterruptedSameTopicSequence() {
        Long userId = 104L;
        var repair = trainingFacade.selectCase(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, null);
        var deposit = trainingFacade.selectCase(RiskTopic.DEPOSIT_RETURN, Difficulty.EASY, null);

        trainingFacade.recordAnswer(userId, null, repair.caseId(), false, AnswerSource.TRAINING);
        trainingFacade.recordAnswer(userId, null, deposit.caseId(), false, AnswerSource.TRAINING);
        trainingFacade.recordAnswer(userId, null, repair.caseId(), false, AnswerSource.TRAINING);

        var mastery = profileFacade.recalculate(userId, RiskTopic.REPAIR_RESPONSIBILITY);
        assertThat(mastery.consecutiveWrong()).isEqualTo(1);
    }
}
