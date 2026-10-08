package com.rentwise.training.dto;

import java.util.List;

public record DiagnosisStartResponse(Long sessionId, Long userId, List<CaseView> cases) {}
