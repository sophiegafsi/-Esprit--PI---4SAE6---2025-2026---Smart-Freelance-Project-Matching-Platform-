package com.example.recompense.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FreelancerRewardInsightDTO {

    private String userEmail;
    private String userName;
    private String currentLevel;
    private String performanceStatus;
    private String nextScoreBadge;
    private Double scoreToNextBadge;
    private String nextPointsBadge;
    private Integer pointsToNextBadge;
    private String nextRecompense;
    private Integer pointsToNextRecompense;
    private Integer eligibleRecompensesCount;
    private Integer lockedRecompensesCount;
    private Integer availableRecompensesCount;
    private List<String> recommendations;
    private List<RewardOpportunityDTO> opportunities;
}
