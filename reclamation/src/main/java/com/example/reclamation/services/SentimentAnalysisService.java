package com.example.reclamation.services;

import com.example.reclamation.entites.Sentiment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SentimentAnalysisService {

    private final List<String> positiveWords = Arrays.asList(
            "merci", "parfait", "excellent", "bien", "super", "génial", "satisfait", "content", "bravo"
    );

    private final List<String> negativeWords = Arrays.asList(
            "problème", "nul", "mauvais", "horrible", "retard", "erreur", "déçu", "insatisfait", "bloqué", "bug"
    );

    public Sentiment detectSentiment(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Sentiment.NEUTRE;
        }

        String lower = message.toLowerCase();

        int positiveScore = countMatches(lower, positiveWords);
        int negativeScore = countMatches(lower, negativeWords);

        if (positiveScore > negativeScore) {
            return Sentiment.POSITIVE;
        }

        if (negativeScore > positiveScore) {
            return Sentiment.NEGATIVE;
        }

        return Sentiment.NEUTRE;
    }

    public String getSentimentReason(String message) {
        Sentiment sentiment = detectSentiment(message);

        return switch (sentiment) {
            case POSITIVE -> "Le message contient un ton positif ou satisfaisant.";
            case NEGATIVE -> "Le message contient un ton négatif ou problématique.";
            case NEUTRE -> "Le message est plutôt neutre.";
        };
    }

    private int countMatches(String text, List<String> words) {
        int count = 0;

        for (String word : words) {
            if (text.contains(word)) {
                count++;
            }
        }

        return count;
    }
}