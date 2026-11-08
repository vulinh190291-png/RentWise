package com.rentwise.profilesvc;

import java.util.List;

public record ProfileSnapshot(
        long snapshotVersion,
        List<TopicFacts> topics,
        List<AnswerFact> recentUserAnswers
) {

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
