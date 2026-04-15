package com.example.evaluation_service.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RewardEvaluationSyncRequest {

    private Long evaluationId;
    private String freelancerEmail;
    private String freelancerName;
    private String projectName;
    private Integer currentScore;
    private Double averageScore;
    private Integer totalPoints;
    private Integer totalEvaluations;
    private Integer positiveEvaluations;
    private Integer completedProjects;
    private LocalDateTime evaluatedAt;
}
