package com.example.recompense.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_badge")
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER)
    private Badge badge;

    private LocalDateTime dateAssigned;

    @Column(name = "is_active")
    private boolean active = true;

    private LocalDateTime revokedAt;

    @Column(length = 500)
    private String statusReason;

    private Long evaluationId;

    private Integer scoreSnapshot;

    private Double averageScoreSnapshot;

    private Integer totalPointsSnapshot;

    private boolean certificateGenerated;
}
