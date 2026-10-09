package com.rentwise.auth.dto;

import com.rentwise.user.domain.UserRole;

public record RegisterResponse(Long id, String username, UserRole role) {}
