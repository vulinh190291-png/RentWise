package com.rentwise.training.repository;

import com.rentwise.training.domain.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Long> {
    Optional<DiagnosisSession> findByIdAndUserId(Long id, Long userId);
}
