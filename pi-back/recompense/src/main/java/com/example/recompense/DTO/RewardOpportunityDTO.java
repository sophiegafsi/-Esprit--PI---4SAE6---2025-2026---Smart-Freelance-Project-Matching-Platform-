package com.example.recompense.DTO;

import lombok.Data;

@Data
public class RewardOpportunityDTO {

    private String type;
    private String title;
    private String description;
    private Double currentValue;
    private Double requiredValue;
    private Double remainingValue;
    private Boolean eligible;
    private Boolean alreadyAwarded;
    private Boolean available;
}
