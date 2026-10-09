package com.rentwise.profile.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.profile.dto.UserProfileResponse;
import com.rentwise.profile.service.ProfileFacade;
import com.rentwise.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileFacade profileFacade;

    public ProfileController(ProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(profileFacade.getProfile(currentUser.id()));
    }
}
