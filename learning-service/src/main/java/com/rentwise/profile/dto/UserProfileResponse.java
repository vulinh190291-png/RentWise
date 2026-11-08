package com.rentwise.profile.dto;

import java.util.List;

public record UserProfileResponse(Long userId, List<TopicMasteryView> topics) {}
