package com.rentwise.training.repository;

import com.rentwise.training.domain.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Long> {}
