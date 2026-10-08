package com.rentwise.profile.repository;

import com.rentwise.profile.domain.UserMastery;
import com.rentwise.training.domain.RiskTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserMasteryRepository extends JpaRepository<UserMastery, Long> {
    Optional<UserMastery> findByUserIdAndTopic(Long userId, RiskTopic topic);
    List<UserMastery> findByUserId(Long userId);
    long countByUserId(Long userId);
}
