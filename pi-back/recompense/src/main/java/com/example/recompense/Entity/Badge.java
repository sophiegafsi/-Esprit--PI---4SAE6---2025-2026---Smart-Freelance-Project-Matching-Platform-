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
@Table(name = "badge")
@Data
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
    private String icon;

    // Supported values: AVERAGE_SCORE, POINTS
    @Column(name = "condition_type")
    private String conditionType;

    // Minimum value required to unlock this badge.
    @Column(name = "condition_value")
    private Double conditionValue;

    private String category;

    @Column(name = "points_reward")
    private Integer pointsReward = 0;

    @Column(name = "auto_assignable")
    private Boolean autoAssignable = true;

    @Column(name = "certificate_eligible")
    private Boolean certificateEligible = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
