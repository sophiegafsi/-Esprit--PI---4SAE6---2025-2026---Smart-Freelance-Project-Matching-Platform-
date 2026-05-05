package com.example.evaluation_service.DTO;


import lombok.Data;

@Data

public class EvaluationEvent {

    private String userEmail;

    private int score;

    private Long evaluationId;

    private String badge;

    public EvaluationEvent(String userEmail, int score, Long id, String badge) {
    }
}
