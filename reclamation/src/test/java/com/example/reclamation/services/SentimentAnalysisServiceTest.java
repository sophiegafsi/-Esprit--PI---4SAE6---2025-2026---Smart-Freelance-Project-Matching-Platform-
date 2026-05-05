package com.example.reclamation.services;

import com.example.reclamation.entites.Sentiment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SentimentAnalysisServiceTest {

    @InjectMocks
    private SentimentAnalysisService sentimentAnalysisService;

    @Test
    @DisplayName("Should detect POSITIVE sentiment accurately")
    void testPositiveSentiment() {
        String message = "Merci pour cet excellent service, je suis très satisfait !";
        Sentiment result = sentimentAnalysisService.detectSentiment(message);
        assertEquals(Sentiment.POSITIVE, result);
    }

    @Test
    @DisplayName("Should detect NEGATIVE sentiment accurately")
    void testNegativeSentiment() {
        String message = "C'est nul, il y a une erreur et je suis très déçu.";
        Sentiment result = sentimentAnalysisService.detectSentiment(message);
        assertEquals(Sentiment.NEGATIVE, result);
    }

    @Test
    @DisplayName("Should detect NEUTRE sentiment accurately")
    void testNeutralSentiment() {
        String message = "Bonjour, je voudrais une information sur le projet.";
        Sentiment result = sentimentAnalysisService.detectSentiment(message);
        assertEquals(Sentiment.NEUTRE, result);
    }

    @Test
    @DisplayName("Should return correct reason for sentiment")
    void testSentimentReason() {
        assertEquals("Le message contient un ton positif ou satisfaisant.", sentimentAnalysisService.getSentimentReason("Bravo !"));
        assertEquals("Le message contient un ton négatif ou problématique.", sentimentAnalysisService.getSentimentReason("C'est horrible."));
    }
}
