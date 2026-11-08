package com.rentwise.training.repository;

import com.rentwise.training.domain.TrainingAnswerSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingAnswerSubmissionRepository
        extends JpaRepository<TrainingAnswerSubmission, Long> {

    Optional<TrainingAnswerSubmission> findByUserIdAndRequestId(
            Long userId,
            String requestId
    );
}
