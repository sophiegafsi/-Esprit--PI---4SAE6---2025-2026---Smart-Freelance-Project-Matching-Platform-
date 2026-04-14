package com.example.reclamation.dto;

import com.example.reclamation.entites.Sentiment;

public class SentimentResultDTO {

    private Sentiment sentiment;
    private String reason;

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}