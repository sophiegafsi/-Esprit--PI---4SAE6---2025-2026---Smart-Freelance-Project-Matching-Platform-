package com.example.recompense.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RewardDashboardDTO {

    private long totalBadgesAssigned;
    private long activeBadges;
    private String mostFrequentBadge;
    private long freelancersWithoutRewardCount;
    private List<String> freelancersWithoutReward = new ArrayList<>();
    private List<TopFreelancerDTO> topFreelancers = new ArrayList<>();
    private List<MonthlyRewardProgressDTO> monthlyProgress = new ArrayList<>();
}
