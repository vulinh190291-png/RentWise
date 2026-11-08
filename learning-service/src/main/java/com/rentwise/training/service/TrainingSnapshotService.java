package com.rentwise.training.service;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.rentwise.common.exception.DomainException;
import com.rentwise.training.domain.RiskTopic;
import com.rentwise.training.dto.AnswerFactDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TrainingSnapshotService {

    private static final int WINDOW_SIZE = 8;

    private final TrainingFacade trainingFacade;

    public TrainingSnapshotService(
            TrainingFacade trainingFacade
    ) {
        this.trainingFacade = trainingFacade;
    }
    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )

    public ProfileSnapshot build(Long userId) {

        List<AnswerFactDto> recent =
                trainingFacade.recentAnswersForUser(
                        userId,
                        WINDOW_SIZE
                );

        if (recent.isEmpty()) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "No answer facts for user " + userId
            );
        }

        // 答题记录 ID 由 MySQL 自增生成。
        // 取最大 ID 作为当前快照的版本。
        long snapshotVersion = recent.stream()
                .mapToLong(AnswerFactDto::answerId)
                .max()
                .orElseThrow();

        List<TopicFacts> topics =
                Arrays.stream(RiskTopic.values())
                        .map(topic -> new TopicFacts(
                                topic.name(),
                                topicAnswers(userId, topic)
                        ))
                        .toList();

        List<AnswerFact> recentUserAnswers =
                recent.stream()
                        .map(this::convert)
                        .toList();

        return new ProfileSnapshot(
                snapshotVersion,
                topics,
                recentUserAnswers
        );
    }

    private List<AnswerFact> topicAnswers(
            Long userId,
            RiskTopic topic
    ) {
        List<AnswerFactDto> records =
                trainingFacade.recentAnswers(
                        userId,
                        topic,
                        WINDOW_SIZE
                );

        if (records.isEmpty()) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "No answer facts for topic " + topic
            );
        }

        return records.stream()
                .map(this::convert)
                .toList();
    }

    private AnswerFact convert(AnswerFactDto fact) {
        return new AnswerFact(
                fact.topic().name(),
                fact.difficulty().name(),
                fact.correct()
        );
    }

    public record ProfileSnapshot(
            long snapshotVersion,
            List<TopicFacts> topics,
            List<AnswerFact> recentUserAnswers
    ) {
    }

    public record TopicFacts(
            String topic,
            List<AnswerFact> answers
    ) {
    }

    public record AnswerFact(
            String topic,
            String difficulty,
            boolean correct
    ) {
    }
}
