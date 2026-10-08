package com.rentwise.training.repository;

import com.rentwise.training.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, Long> {
    List<AnswerRecord> findByUserIdAndTopicOrderByAnsweredAtDescIdDesc(Long userId, RiskTopic topic, Pageable pageable);
    List<AnswerRecord> findByUserIdOrderByAnsweredAtDescIdDesc(Long userId, Pageable pageable);
    long countBySessionId(Long sessionId);
    boolean existsBySessionIdAndCaseId(Long sessionId, Long caseId);
}
