package com.example.evaluation_service.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiRestService {

    @Value("${gemini.api-key:AIzaSyBmaN-2aFMWVc56tIZEPFsY7D8UNZEmRy8}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String analyzeSentiment(String text) {
        // LOG 1 : Vérifier que le service est bien appelé
        System.out.println("🔍 [GEMINI] analyzeSentiment() appelé avec: \"" + text + "\"");

        if (text == null || text.trim().isEmpty()) {
            System.out.println("⚠️ [GEMINI] Texte vide -> NEUTRE");
            return "NEUTRE";
        }


        if (cache.containsKey(text)) {
            System.out.println("📦 [GEMINI] Cache hit: " + cache.get(text));
            return cache.get(text);
        }

        try {
            // LOG 3 : Appel API
            System.out.println("🌐 [GEMINI] Appel API avec clé: " + apiKey.substring(0, 10) + "...");
            String sentiment = callGemini(text);
            System.out.println("✅ [GEMINI] Réponse API: " + sentiment);

            cache.put(text, sentiment);
            return sentiment;

        } catch (HttpClientErrorException.TooManyRequests e) {
            // Gestion spécifique du quota dépassé
            System.err.println("❌ [GEMINI] QUOTA DÉPASSÉ (429): " + e.getMessage());
            System.err.println("⚠️ [GEMINI] Utilisation du fallback local");

            String fallback = analyzeLocally(text);
            cache.put(text, fallback);
            return fallback;

        } catch (Exception e) {
            System.err.println("❌ [GEMINI] Erreur: " + e.getMessage());
            e.printStackTrace();

            String fallback = analyzeLocally(text);
            cache.put(text, fallback);
            return fallback;
        }
    }

    private String callGemini(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text",
                                "Tu es un expert en analyse de sentiment. " +
                                        "Analyse le sentiment de ce commentaire en français. " +
                                        "Réponds UNIQUEMENT par un des trois mots: POSITIF, NEUTRE ou NEGATIF.\n\n" +
                                        "Commentaire: " + text)
                })
        });

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        System.out.println("📡 [GEMINI] URL: " + url);

        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        System.out.println("📦 [GEMINI] Réponse brute: " + response);

        return extractSentiment(response);
    }

    private String extractSentiment(Map<String, Object> response) {
        try {
            var candidates = (java.util.List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                System.out.println("⚠️ [GEMINI] Pas de candidats dans la réponse");
                return "NEUTRE";
            }

            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (java.util.List<Map<String, Object>>) content.get("parts");
            String result = (String) parts.get(0).get("text");

            System.out.println("📝 [GEMINI] Résultat brut: " + result);

            result = result.trim().toUpperCase();
            if (result.contains("POSITIF")) return "POSITIF";
            if (result.contains("NEGATIF")) return "NEGATIF";
            return "NEUTRE";

        } catch (Exception e) {
            System.err.println("❌ [GEMINI] Erreur extraction: " + e.getMessage());
            return "NEUTRE";
        }
    }

    // Fallback local si l'API ne répond pas
    private String analyzeLocally(String text) {
        System.out.println("🔄 [FALLBACK] Analyse locale pour: \"" + text + "\"");

        String lower = text.toLowerCase();


        if (lower.contains("excellent") || lower.contains("super") ||
                lower.contains("merci") || lower.contains("bravo") ||
                lower.contains("satisfait") || lower.contains("parfait") ||
                lower.contains("génial") || lower.contains("top") ||
                lower.contains("bien") || lower.contains("bon")) {
            return "POSITIF";
        }

        // Mots négatifs
        if (lower.contains("mauvais") || lower.contains("déçu") ||
                lower.contains("problème") || lower.contains("nul") ||
                lower.contains("horrible") || lower.contains("désolé") ||
                lower.contains("dommage") || lower.contains("raté") ||
                lower.contains("erreur") || lower.contains("pas bien")) {
            return "NEGATIF";
        }

        return "NEUTRE";
    }

    public void clearCache() {
        cache.clear();
        System.out.println("🧹 [GEMINI] Cache vidé");
    }

    public int getCacheSize() {
        return cache.size();
    }
}