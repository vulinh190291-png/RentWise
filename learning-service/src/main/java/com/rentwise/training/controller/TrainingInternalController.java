package com.rentwise.training.controller;

import com.rentwise.training.service.TrainingSnapshotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/training")
public class TrainingInternalController {

    private final TrainingSnapshotService snapshotService;
    private final String internalKey;

    public TrainingInternalController(
            TrainingSnapshotService snapshotService,
            @Value("${rentwise.internal-key}") String internalKey) {

        if (internalKey.isBlank()) {
            throw new IllegalArgumentException(
                    "INTERNAL_API_KEY must not be blank"
            );
        }

        this.snapshotService = snapshotService;
        this.internalKey = internalKey;
    }

    @GetMapping("/users/{userId}/snapshot")
    public TrainingSnapshotService.ProfileSnapshot snapshot(
            @RequestHeader(
                    value = "X-Internal-Key",
                    required = false
            ) String key,
            @PathVariable Long userId) {

        verifyKey(key);

        if (userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid userId"
            );
        }

        return snapshotService.build(userId);
    }

    private void verifyKey(String key) {
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
