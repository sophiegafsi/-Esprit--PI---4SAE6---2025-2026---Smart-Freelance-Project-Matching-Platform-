package com.example.recompense.DTO;





import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationEvent {

    private Long evaluationId;
    private String freelancerEmail;
    private String freelancerName;
    private String projectName;
    private Integer currentScore;
    private Double averageScore;
    private Integer totalPoints;
    private Integer totalEvaluations;
    private Integer positiveEvaluations;
    private Integer completedProjects;
}

