package com.example.recompense.DTO;

import lombok.Data;

@Data
public class RewardProcessingResponse {

    private String freelancerEmail;
    private String freelancerName;
    private Double averageScore;
    private Integer totalPoints;
    private String currentScoreBadge;
    private String currentPointsBadge;
    private String currentLevel;
    private String message;
}
