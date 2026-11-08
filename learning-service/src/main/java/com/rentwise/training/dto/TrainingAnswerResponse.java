package com.rentwise.training.dto;

import com.rentwise.plan.dto.NextTrainingResponse;
import com.rentwise.profile.dto.TopicMasteryView;

public record TrainingAnswerResponse(AnswerResult answer, FeedbackView feedback,
                                     TopicMasteryView updatedMastery, NextTrainingResponse nextTraining) {}
