package com.example.evaluation_service.DTO;

public class SentimentRequestDTO {
    private String text;

    public SentimentRequestDTO() {}

    public SentimentRequestDTO(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}