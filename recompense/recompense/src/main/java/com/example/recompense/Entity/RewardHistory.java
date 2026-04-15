package com.example.recompense.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_history")
@Data
public class RewardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    private String userName;

    @Column(nullable = false)
    private String rewardName;

    @Column(nullable = false)
    private String rewardType;

    @Column(nullable = false)
    private String actionType;

    @Column(length = 1000)
    private String description;

    private Long evaluationId;

    private Integer scoreSnapshot;

    private Double averageScoreSnapshot;

    private Integer totalPointsSnapshot;

    private Integer totalEvaluationsSnapshot;

    private Integer positiveEvaluationsSnapshot;

    private Integer completedProjectsSnapshot;

    private Boolean certificateGenerated = false;

    private LocalDateTime eventDate = LocalDateTime.now();
}
