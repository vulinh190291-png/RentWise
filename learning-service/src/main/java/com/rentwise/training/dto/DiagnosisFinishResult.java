package com.rentwise.training.dto;

public record DiagnosisFinishResult(Long sessionId, Long userId, boolean alreadyFinished) {}
