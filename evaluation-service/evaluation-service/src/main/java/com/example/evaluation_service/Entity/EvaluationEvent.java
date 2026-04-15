package com.example.evaluation_service.Entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class EvaluationEvent {

    private String userEmail;
    private int score;
    private Long evaluationId;
    private String badge;

    public EvaluationEvent() {}

    public EvaluationEvent(String userEmail, int score, Long evaluationId, String badge) {
        this.userEmail = userEmail;
        this.score = score;
        this.evaluationId = evaluationId;
        this.badge = badge;
    }

    // getters & setters
}
