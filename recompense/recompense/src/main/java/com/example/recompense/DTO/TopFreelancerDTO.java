package com.example.recompense.DTO;

import lombok.Data;

@Data
public class TopFreelancerDTO {

    private String userEmail;
    private String userName;
    private Double averageScore;
    private Integer totalPoints;
    private Integer totalEvaluations;
    private Integer positiveEvaluations;
    private Integer completedProjects;
    private String currentLevel;
    private String currentScoreBadge;
    private String currentPointsBadge;
}
