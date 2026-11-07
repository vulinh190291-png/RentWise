package com.rentwise.profilesvc;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ProfileSnapshotService {

    private static final int WINDOW_SIZE = 8;

    private static final List<String> TOPICS = List.of(
            "DEPOSIT_RETURN",
            "EARLY_TERMINATION",
            "REPAIR_RESPONSIBILITY",
            "COST_BEARING",
            "BREACH_LIABILITY"
    );

    private static final Map<String, String> NAMES = Map.of(
            "DEPOSIT_RETURN", "押金返还",
            "EARLY_TERMINATION", "提前退租",
            "REPAIR_RESPONSIBILITY", "维修责任",
            "COST_BEARING", "费用承担",
            "BREACH_LIABILITY", "违约责任"
    );

    private final UserMasteryRepository repository;

    public ProfileSnapshotService(
            UserMasteryRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public ProfileView sync(
            Long userId,
            ProfileSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        Map<String, ProfileSnapshot.TopicFacts> byTopic =
                new HashMap<>();

        for (var item : snapshot.topics()) {
            byTopic.put(item.topic(), item);
        }

        for (String topic : TOPICS) {
            List<ProfileSnapshot.AnswerFact> answers =
                    byTopic.get(topic).answers();

            int score = calculateScore(answers);

            int consecutiveWrong = calculateConsecutiveWrong(
                    topic,
                    snapshot.recentUserAnswers()
            );

            String lastDifficulty =
                    answers.get(0).difficulty();

            UserMastery mastery = repository
                    .findByUserIdAndTopic(userId, topic)
                    .orElseGet(
                            () -> new UserMastery(userId, topic)
                    );

            mastery.update(
                    score,
                    consecutiveWrong,
                    lastDifficulty
            );

            repository.save(mastery);
        }

        return getProfile(userId);
    }

    @Transactional(readOnly = true)
    public ProfileView getProfile(Long userId) {
        List<UserMastery> rows =
                repository.findByUserId(userId);

        if (rows.size() != TOPICS.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Profile not initialized"
            );
        }

        Map<String, UserMastery> byTopic =
                new HashMap<>();

        for (UserMastery row : rows) {
            byTopic.put(row.getTopic(), row);
        }

        List<TopicView> topics = new ArrayList<>();

        for (String topic : TOPICS) {
            UserMastery row = byTopic.get(topic);

            if (row == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Missing profile topic"
                );
            }

            topics.add(new TopicView(
                    topic,
                    NAMES.get(topic),
                    row.getMasteryScore(),
                    level(row.getMasteryScore()),
                    row.getConsecutiveWrong(),
                    row.getLastDifficulty()
            ));
        }

        return new ProfileView(userId, topics);
    }

    private void validateSnapshot(
            ProfileSnapshot snapshot
    ) {
        if (snapshot == null
                || snapshot.topics() == null
                || snapshot.recentUserAnswers() == null
                || snapshot.topics().size() != TOPICS.size()) {
            badRequest("Five topic snapshots required");
        }

        Set<String> seen = new HashSet<>();

        for (var item : snapshot.topics()) {
            if (item == null
                    || !TOPICS.contains(item.topic())
                    || !seen.add(item.topic())
                    || item.answers() == null
                    || item.answers().isEmpty()
                    || item.answers().size() > WINDOW_SIZE) {
                badRequest("Invalid topic snapshot");
            }

            for (var answer : item.answers()) {
                validateAnswer(answer);

                if (!item.topic().equals(answer.topic())) {
                    badRequest("Answer topic mismatch");
                }
            }
        }

        if (snapshot.recentUserAnswers().size()
                > WINDOW_SIZE) {
            badRequest("Too many recent user answers");
        }

        for (var answer : snapshot.recentUserAnswers()) {
            validateAnswer(answer);
        }
    }

    private void validateAnswer(
            ProfileSnapshot.AnswerFact answer
    ) {
        if (answer == null
                || !TOPICS.contains(answer.topic())
                || !List.of("EASY", "MEDIUM", "HARD")
                .contains(answer.difficulty())) {
            badRequest("Invalid answer fact");
        }
    }

    private int calculateScore(
            List<ProfileSnapshot.AnswerFact> answers
    ) {
        int totalWeight = 0;
        int correctWeight = 0;

        for (var answer : answers) {
            int weight = switch (answer.difficulty()) {
                case "EASY" -> 1;
                case "MEDIUM" -> 2;
                case "HARD" -> 3;
                default -> throw new IllegalStateException();
            };

            totalWeight += weight;

            if (answer.correct()) {
                correctWeight += weight;
            }
        }

        return (int) Math.round(
                correctWeight * 100.0 / totalWeight
        );
    }

    private int calculateConsecutiveWrong(
            String topic,
            List<ProfileSnapshot.AnswerFact> recent
    ) {
        int count = 0;

        for (var answer : recent) {
            if (!topic.equals(answer.topic())
                    || answer.correct()) {
                break;
            }

            count++;
        }

        return count;
    }

    private String level(int score) {
        if (score < 50) {
            return "WEAK";
        }
        if (score < 80) {
            return "NORMAL";
        }
        return "MASTERED";
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    public record TopicView(
            String topic,
            String topicName,
            int masteryScore,
            String level,
            int consecutiveWrong,
            String lastDifficulty
    ) {
    }

    public record ProfileView(
            Long userId,
            List<TopicView> topics
    ) {
    }
}
