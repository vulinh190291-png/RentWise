package com.rentwise.training.repository;

import com.rentwise.training.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LearningCardRepository extends JpaRepository<LearningCard, Long> {
    Optional<LearningCard> findByTopic(RiskTopic topic);
}
