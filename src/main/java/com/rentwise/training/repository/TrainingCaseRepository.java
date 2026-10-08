package com.rentwise.training.repository;

import com.rentwise.training.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingCaseRepository extends JpaRepository<TrainingCase, Long> {
    List<TrainingCase> findByDiagnosisEligibleTrueAndActiveTrueOrderByIdAsc();
    List<TrainingCase> findByTopicAndDifficultyAndActiveTrueOrderByIdAsc(RiskTopic topic, Difficulty difficulty);
    List<TrainingCase> findByTopicAndActiveTrueOrderByIdAsc(RiskTopic topic);
}
