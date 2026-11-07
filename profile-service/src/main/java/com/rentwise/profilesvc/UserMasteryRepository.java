package com.rentwise.profilesvc;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserMasteryRepository
        extends JpaRepository<UserMastery, Long> {

    Optional<UserMastery> findByUserIdAndTopic(
            Long userId,
            String topic
    );

    List<UserMastery> findByUserId(Long userId);
}
