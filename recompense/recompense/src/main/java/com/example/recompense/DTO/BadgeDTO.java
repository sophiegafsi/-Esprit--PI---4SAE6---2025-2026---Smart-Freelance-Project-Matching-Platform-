package com.example.recompense.DTO;



import lombok.Data;

@Data
public class BadgeDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String conditionType;
    private Double conditionValue;
    private String category;
    private Integer pointsReward;
    private Boolean autoAssignable;
    private Boolean certificateEligible;
    private Boolean isActive;
}
