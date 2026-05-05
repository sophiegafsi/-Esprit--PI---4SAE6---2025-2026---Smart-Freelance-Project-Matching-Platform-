package com.example.recompense.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBadgeDTO {
    private String badgeName;
    private String description;
    private String icon;
    private LocalDateTime dateAssigned;
    private boolean active;
    private String statusReason;
    private boolean certificateGenerated;
    private String conditionType;
    private Double conditionValue;
}
