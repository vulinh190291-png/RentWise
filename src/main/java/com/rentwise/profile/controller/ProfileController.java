package com.rentwise.profile.controller;

import com.rentwise.common.response.ApiResponse;
import com.rentwise.profile.dto.UserProfileResponse;
import com.rentwise.profile.service.ProfileFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileFacade profileFacade;
    public ProfileController(ProfileFacade profileFacade) { this.profileFacade = profileFacade; }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> profile(@PathVariable Long userId) {
        return ApiResponse.ok(profileFacade.getProfile(userId));
    }
}
