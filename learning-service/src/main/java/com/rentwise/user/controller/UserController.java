package com.rentwise.user.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.security.CurrentUser;
import com.rentwise.user.dto.CurrentUserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(new CurrentUserResponse(
                currentUser.id(),
                currentUser.username(),
                currentUser.role()));
    }
}
