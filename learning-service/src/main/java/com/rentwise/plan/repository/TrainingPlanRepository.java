package com.rentwise.plan.repository;

import com.rentwise.plan.domain.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    Optional<TrainingPlan> findByUserId(Long userId);
    long countByUserId(Long userId);
}
