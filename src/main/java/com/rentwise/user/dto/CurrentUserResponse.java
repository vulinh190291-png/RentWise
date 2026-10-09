package com.rentwise.user.dto;

import com.rentwise.user.domain.UserRole;

public record CurrentUserResponse(Long id, String username, UserRole role) {}
