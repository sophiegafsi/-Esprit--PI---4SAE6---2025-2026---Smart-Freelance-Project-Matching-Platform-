package com.example.reclamation.services;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ReponseModerationService {

    private final List<String> badWords = Arrays.asList(
            "idiot", "stupide", "nul", "merde", "fuck", "shit", "imbecile", "con"
    );

    public boolean containsBadWords(String message) {
        if (message == null) {
            return false;
        }

        String lower = message.toLowerCase();
        return badWords.stream().anyMatch(lower::contains);
    }

    public String detectBadWord(String message) {
        if (message == null) {
            return null;
        }

        String lower = message.toLowerCase();

        return badWords.stream()
                .filter(lower::contains)
                .findFirst()
                .orElse(null);
    }

    public String suggestProfessionalMessage(String message) {
        return "Nous vous remercions pour votre message. Nous comprenons votre situation et nous allons traiter votre demande dans les plus brefs délais.";
    }
}