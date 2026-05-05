package com.example.recompense.DTO;

import lombok.Data;

@Data
public class MonthlyRewardProgressDTO {

    private String month;
    private long awardedCount;
    private long revokedCount;
}
