package tn.esprit.GestionPortfolio.Services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.GestionPortfolio.DTO.TextAssistantRequest;
import tn.esprit.GestionPortfolio.DTO.TextAssistantResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AiTextAssistantService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final List<String> BLOCKED_WORDS = List.of("shit", "fuck", "damn", "idiot", "merde");

    private static final Map<String, String> ENGLISH_GLOSSARY = createEnglishGlossary();

    private final ObjectMapper objectMapper;

    @Value("${portfolio.ai.translation.enabled:true}")
    private boolean translationEnabled;

    @Value("${portfolio.ai.translation.base-url:https://translate.googleapis.com/translate_a/single}")
    private String translationBaseUrl;

    @Value("${portfolio.ai.translation.timeout-ms:5000}")
    private int translationTimeoutMs;

    @Value("${portfolio.ai.rewrite.correction.enabled:true}")
    private boolean rewriteCorrectionEnabled;

    @Value("${portfolio.ai.rewrite.correction.base-url:https://api.languagetool.org/v2/check}")
    private String rewriteCorrectionBaseUrl;

    @Value("${portfolio.ai.rewrite.correction.timeout-ms:5000}")
    private int rewriteCorrectionTimeoutMs;

    public AiTextAssistantService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TextAssistantResponse rewriteText(TextAssistantRequest request) {
        String originalText = requireText(request);
        String rewritten = professionalizeTextPreserveLanguage(originalText);

        return new TextAssistantResponse(
                originalText,
                rewritten,
                "rewrite",
            "UNCHANGED",
                !rewritten.equals(originalText)
        );
    }

    public TextAssistantResponse translateText(TextAssistantRequest request) {
        String originalText = requireText(request);
        String targetLanguage = normalizeTargetLanguage(request == null ? null : request.targetLanguage());
        String targetLanguageCode = resolveTargetLanguageCode(targetLanguage);

        String translated = translateTextInternal(originalText, targetLanguageCode, targetLanguage);
        return new TextAssistantResponse(
                originalText,
                translated,
                "translate",
                targetLanguage,
                !translated.equals(originalText)
        );
    }

    public TextAssistantResponse maskBadWords(TextAssistantRequest request) {
        String originalText = requireText(request);
        String masked = maskBlockedWords(originalText);

        return new TextAssistantResponse(
                originalText,
                masked,
                "mask-bad-words",
                "UNCHANGED",
                !masked.equals(originalText)
        );
    }

    public String maskBlockedWords(String text) {
        String masked = normalizeWhitespace(text);
        for (String word : BLOCKED_WORDS) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b");
            masked = pattern.matcher(masked).replaceAll(match -> "*".repeat(match.group().length()));
        }
        return masked;
    }

    private String professionalizeTextPreserveLanguage(String text) {
        String normalized = finalizeSentence(text);

        normalized = correctGrammarAndSpelling(normalized);

        normalized = normalized
            .replaceAll("(?i)\\bi made\\b", "I designed and delivered")
            .replaceAll("(?i)\\bi did\\b", "I contributed to")
            .replaceAll("(?i)\\bi worked on\\b", "I contributed to")
            .replaceAll("(?i)\\bi built\\b", "I developed")
            .replaceAll("(?i)\\bapp\\b", "application")
            .replaceAll("(?i)\\bwebsite\\b", "web platform")
            .replaceAll("(?i)\\bgonna\\b", "going to")
                .replaceAll("(?i)\\bwanna\\b", "want to")
                .replaceAll("(?i)\\bj'ai fait\\b", "j'ai realise")
                .replaceAll("(?i)\\bj ai fait\\b", "j'ai realise")
                .replaceAll("(?i)\\bon a fait\\b", "nous avons realise")
                .replaceAll("(?i)\\bj'ai travaille sur\\b", "j'ai contribue a")
                .replaceAll("(?i)\\bj ai travaille sur\\b", "j'ai contribue a")
                .replaceAll("(?i)\\bsite web\\b", "plateforme web");

        normalized = normalizeAcronyms(normalized);
        normalized = capitalizeFirst(normalized);

        return maskBlockedWords(finalizeSentence(normalized));
    }

    private String correctGrammarAndSpelling(String text) {
        if (!rewriteCorrectionEnabled) {
            return text;
        }

        String source = normalizeWhitespace(text);
        if (source.isBlank()) {
            return source;
        }

        try {
            return applyLanguageToolCorrections(source);
        } catch (Exception ignored) {
            // Keep rewrite available even if grammar API is temporarily unavailable.
            return source;
        }
    }

    private String applyLanguageToolCorrections(String text) throws Exception {
        String formBody = "language=" + URLEncoder.encode("auto", StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rewriteCorrectionBaseUrl))
                .timeout(Duration.ofMillis(Math.max(1000, rewriteCorrectionTimeoutMs)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Grammar correction provider unavailable.");
        }

        return applyCorrectionMatches(text, response.body());
    }

    private String applyCorrectionMatches(String source, String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(String.valueOf(rawJson == null ? "" : rawJson).trim());
        JsonNode matches = root.path("matches");
        if (!matches.isArray() || matches.isEmpty()) {
            return source;
        }

        List<CorrectionSpan> spans = new ArrayList<>();
        for (JsonNode match : matches) {
            int offset = match.path("offset").asInt(-1);
            int length = match.path("length").asInt(0);
            JsonNode replacements = match.path("replacements");
            if (offset < 0 || length <= 0 || !replacements.isArray() || replacements.isEmpty()) {
                continue;
            }

            String replacement = replacements.get(0).path("value").asText("").trim();
            if (replacement.isBlank()) {
                continue;
            }

            int end = offset + length;
            if (offset >= source.length() || end > source.length()) {
                continue;
            }

            spans.add(new CorrectionSpan(offset, end, replacement));
        }

        if (spans.isEmpty()) {
            return source;
        }

        spans.sort(Comparator.comparingInt(CorrectionSpan::start).reversed());
        StringBuilder patched = new StringBuilder(source);
        int lastAppliedStart = Integer.MAX_VALUE;

        for (CorrectionSpan span : spans) {
            if (span.end() > lastAppliedStart) {
                continue;
            }
            patched.replace(span.start(), span.end(), span.replacement());
            lastAppliedStart = span.start();
        }

        return patched.toString();
    }

    private record CorrectionSpan(int start, int end, String replacement) {
    }

        private String normalizeAcronyms(String value) {
        return String.valueOf(value == null ? "" : value)
            .replaceAll("(?i)\\bai\\b", "AI")
            .replaceAll("(?i)\\bapi\\b", "API")
            .replaceAll("(?i)\\bui\\b", "UI")
            .replaceAll("(?i)\\bux\\b", "UX")
            .replaceAll("(?i)\\bsql\\b", "SQL")
            .replaceAll("(?i)\\baws\\b", "AWS");
        }

    private String translateTextInternal(String text, String targetLanguageCode, String targetLanguageLabel) {
        String cleanText = normalizeWhitespace(text);
        if (cleanText.isBlank()) {
            return cleanText;
        }

        if (translationEnabled) {
            try {
                String translated = translateWithApi(cleanText, targetLanguageCode);
                if (!translated.isBlank()) {
                    return finalizeSentence(translated);
                }
            } catch (Exception ignored) {
                // Fallback to local lightweight translation when external service is unavailable.
            }
        }

        return fallbackTranslation(cleanText, targetLanguageCode, targetLanguageLabel);
    }

    private String translateWithApi(String text, String targetLanguageCode) throws Exception {
        String query = "client=gtx&sl=auto&tl="
                + URLEncoder.encode(targetLanguageCode, StandardCharsets.UTF_8)
                + "&dt=t&q="
                + URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(translationBaseUrl + "?" + query))
                .timeout(Duration.ofMillis(Math.max(1000, translationTimeoutMs)))
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Translation provider unavailable.");
        }

        return parseTranslatedText(response.body());
    }

    private String parseTranslatedText(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(String.valueOf(rawJson == null ? "" : rawJson).trim());
        if (!root.isArray() || root.isEmpty()) {
            return "";
        }

        JsonNode translatedParts = root.get(0);
        if (!translatedParts.isArray()) {
            return "";
        }

        StringBuilder merged = new StringBuilder();
        for (JsonNode part : translatedParts) {
            if (part.isArray() && !part.isEmpty()) {
                String chunk = part.get(0).asText("").trim();
                if (!chunk.isBlank()) {
                    if (!merged.isEmpty()) {
                        merged.append(' ');
                    }
                    merged.append(chunk);
                }
            }
        }
        return merged.toString().trim();
    }

    private String fallbackTranslation(String text, String targetLanguageCode, String targetLanguageLabel) {
        if ("en".equalsIgnoreCase(targetLanguageCode) || "ENGLISH".equalsIgnoreCase(targetLanguageLabel)) {
            return finalizeSentence(translateToEnglishInternal(text));
        }
        return text;
    }

    private String translateToEnglishInternal(String text) {
        String translated = " " + normalizeForTranslation(text) + " ";
        translated = translated
            .replace(" on va a lecole ", " we go to school ")
            .replace(" on va a l ecole ", " we go to school ")
            .replace(" aller a lecole ", " go to school ")
            .replace(" aller a l ecole ", " go to school ")
                .replace(" tableau de bord ", " dashboard ")
                .replace(" base de donnees ", " database ")
                .replace(" base de données ", " database ")
                .replace(" gestion des commandes ", " order management ")
                .replace(" application mobile ", " mobile application ");

        for (Map.Entry<String, String> entry : ENGLISH_GLOSSARY.entrySet()) {
            translated = translated.replace(" " + entry.getKey() + " ", " " + entry.getValue() + " ");
        }

        translated = translated
                .replaceAll("\\bexperience utilisateur\\b", "user experience")
                .replaceAll("\\bgestion des\\b", "management of")
                .replaceAll("\\bgestion de\\b", "management of");

        translated = translated.trim();
        translated = translated.replaceAll("\\s+", " ");
        translated = translated.replaceAll(" +([,.;:!?])", "$1");
        return capitalizeFirst(translated);
    }

    private String normalizeForTranslation(String value) {
        String normalized = Normalizer.normalize(String.valueOf(value == null ? "" : value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        normalized = normalized
                .toLowerCase()
                .replaceAll("['’]", " ")
                .replaceAll("([,.;:!?])", " $1 ")
                .replaceAll("[^a-z0-9,.;:!?\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }

    private String requireText(TextAssistantRequest request) {
        String text = request == null ? "" : String.valueOf(request.text() == null ? "" : request.text()).trim();
        if (text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text must not be blank.");
        }
        return text;
    }

    private String normalizeTargetLanguage(String targetLanguage) {
        String normalized = String.valueOf(targetLanguage == null ? "ENGLISH" : targetLanguage).trim().toUpperCase();
        if (normalized.isBlank()) {
            return "ENGLISH";
        }
        return normalized;
    }

    private String resolveTargetLanguageCode(String targetLanguage) {
        return switch (targetLanguage) {
            case "ENGLISH" -> "en";
            case "FRENCH" -> "fr";
            case "SPANISH" -> "es";
            case "GERMAN" -> "de";
            case "ITALIAN" -> "it";
            case "PORTUGUESE" -> "pt";
            case "ARABIC" -> "ar";
            case "TURKISH" -> "tr";
            default -> {
                if (targetLanguage.matches("^[A-Z]{2}(-[A-Z]{2})?$")) {
                    yield targetLanguage.toLowerCase();
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported targetLanguage. Use a name (ENGLISH, FRENCH, ARABIC...) or an ISO code (en, fr, ar...).");
            }
        };
    }

    private String normalizeWhitespace(String value) {
        return String.valueOf(value == null ? "" : value).replaceAll("\\s+", " ").trim();
    }

    private String finalizeSentence(String value) {
        String cleaned = normalizeWhitespace(value);
        if (cleaned.isBlank()) {
            return cleaned;
        }
        cleaned = capitalizeFirst(cleaned);
        if (!cleaned.matches(".*[.!?]$")) {
            cleaned = cleaned + ".";
        }
        return cleaned;
    }

    private String capitalizeFirst(String value) {
        String cleaned = normalizeWhitespace(value);
        if (cleaned.isBlank()) {
            return cleaned;
        }
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }

    private static Map<String, String> createEnglishGlossary() {
        Map<String, String> glossary = new LinkedHashMap<>();
        glossary.put("developpement", "development");
        glossary.put("développement", "development");
        glossary.put("application", "application");
        glossary.put("plateforme", "platform");
        glossary.put("livraison", "delivery");
        glossary.put("gestion", "management");
        glossary.put("commandes", "orders");
        glossary.put("suivi", "tracking");
        glossary.put("creation", "creation");
        glossary.put("création", "creation");
        glossary.put("analytique", "analytics");
        glossary.put("vente", "sales");
        glossary.put("realisation", "delivery");
        glossary.put("réalisation", "delivery");
        glossary.put("competence", "skill");
        glossary.put("compétence", "skill");
        glossary.put("competences", "skills");
        glossary.put("compétences", "skills");
        glossary.put("projet", "project");
        glossary.put("utilisateur", "user");
        glossary.put("mobile", "mobile");
        glossary.put("dashboard", "dashboard");
        glossary.put("e-commerce", "e-commerce");
        glossary.put("backend", "backend");
        glossary.put("frontend", "frontend");
        glossary.put("api", "api");
        glossary.put("rest", "rest");
        glossary.put("donnees", "data");
        glossary.put("données", "data");
        glossary.put("performance", "performance");
        glossary.put("on", "we");
        glossary.put("va", "go");
        glossary.put("aller", "go");
        glossary.put("a", "to");
        glossary.put("ecole", "school");
        glossary.put("lecole", "school");
        return glossary;
    }
}
