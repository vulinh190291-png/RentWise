package com.rentwise.security;

import com.rentwise.user.domain.UserRole;

public record CurrentUser(Long id, String username, UserRole role) {}
