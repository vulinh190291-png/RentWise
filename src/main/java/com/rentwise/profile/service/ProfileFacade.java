package com.rentwise.profile.service;

import com.rentwise.profile.dto.*;
import com.rentwise.training.domain.RiskTopic;

public interface ProfileFacade {
    UserProfileResponse initializeFromDiagnosis(Long userId);
    TopicMasteryView recalculate(Long userId, RiskTopic topic);
    UserProfileResponse getProfile(Long userId);
}
