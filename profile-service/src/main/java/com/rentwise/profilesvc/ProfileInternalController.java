package com.rentwise.profilesvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/profiles")
public class ProfileInternalController {

    private final ProfileSnapshotService service;
    private final String internalKey;

    public ProfileInternalController(
            ProfileSnapshotService service,
            @Value("${rentwise.internal-key}") String internalKey
    ) {
        if (internalKey.isBlank()) {
            throw new IllegalArgumentException(
                    "INTERNAL_API_KEY must not be blank"
            );
        }

        this.service = service;
        this.internalKey = internalKey;
    }

    @PostMapping("/{userId}/sync")
    public ProfileSnapshotService.ProfileView sync(
            @RequestHeader(value = "X-Internal-Key", required = false)
            String key,
            @PathVariable Long userId,
            @RequestBody ProfileSnapshot snapshot
    ) {
        verify(key);
        return service.sync(userId, snapshot);
    }

    @GetMapping("/{userId}")
    public ProfileSnapshotService.ProfileView get(
            @RequestHeader(value = "X-Internal-Key", required = false)
            String key,
            @PathVariable Long userId
    ) {
        verify(key);
        return service.getProfile(userId);
    }

    private void verify(String key) {
        byte[] expected = internalKey.getBytes(
                StandardCharsets.UTF_8
        );

        byte[] actual = (key == null ? "" : key)
                .getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expected, actual)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid internal credentials"
            );
        }
    }
}
