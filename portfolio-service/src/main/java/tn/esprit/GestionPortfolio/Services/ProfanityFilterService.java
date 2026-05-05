package tn.esprit.GestionPortfolio.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProfanityFilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfanityFilterService.class);

    private static final List<String> FALLBACK_WORDS = List.of(
            "shit",
            "fuck",
            "idiot",
            "merde",
            "putain"
    );

    private final List<ProfanityRule> rules;

    public ProfanityFilterService(
            @Value("${portfolio.ai.bad-words.dictionary-location:classpath:bad-words-dictionary.txt}") String dictionaryLocation,
            @Value("${portfolio.ai.bad-words.custom:}") String customWords,
            ResourceLoader resourceLoader
    ) {
        Set<String> lexicon = new LinkedHashSet<>(FALLBACK_WORDS);
        lexicon.addAll(loadWordsFromResource(dictionaryLocation, resourceLoader));
        lexicon.addAll(parseCustomWords(customWords));

        this.rules = lexicon.stream()
                .map(this::normalizeLexiconEntry)
                .filter(value -> !value.isBlank())
                .distinct()
                .map(this::buildRule)
                .sorted(Comparator.comparingInt((ProfanityRule rule) -> rule.canonical().length()).reversed())
                .toList();
    }

    public String mask(String text) {
        String source = String.valueOf(text == null ? "" : text);
        if (source.isBlank() || rules.isEmpty()) {
            return source;
        }

        boolean[] maskedPositions = new boolean[source.length()];
        boolean found = false;

        for (ProfanityRule rule : rules) {
            Matcher matcher = rule.pattern().matcher(source);
            while (matcher.find()) {
                found = true;
                for (int i = matcher.start(); i < matcher.end(); i++) {
                    if (!Character.isWhitespace(source.charAt(i))) {
                        maskedPositions[i] = true;
                    }
                }
            }
        }

        if (!found) {
            return source;
        }

        StringBuilder masked = new StringBuilder(source.length());
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            masked.append(maskedPositions[i] ? '*' : current);
        }
        return masked.toString();
    }

    private List<String> loadWordsFromResource(String dictionaryLocation, ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(dictionaryLocation);
        if (!resource.exists()) {
            LOGGER.warn("Bad words dictionary not found at {}", dictionaryLocation);
            return List.of();
        }

        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isBlank() && !trimmed.startsWith("#")) {
                    words.add(trimmed);
                }
            }
        } catch (IOException exception) {
            LOGGER.warn("Unable to load bad words dictionary from {}", dictionaryLocation, exception);
        }
        return words;
    }

    private List<String> parseCustomWords(String customWords) {
        String source = String.valueOf(customWords == null ? "" : customWords);
        if (source.isBlank()) {
            return List.of();
        }

        return Pattern.compile("[,;\\r\\n]+")
                .splitAsStream(source)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String normalizeLexiconEntry(String value) {
        String normalized = Normalizer.normalize(String.valueOf(value == null ? "" : value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('\u2019', '\'')
                .replace('-', ' ')
                .replace('\'', ' ')
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }

    private ProfanityRule buildRule(String word) {
        return new ProfanityRule(word, Pattern.compile(buildRegex(word), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
    }

    private String buildRegex(String word) {
        StringBuilder regex = new StringBuilder("(?<![\\p{L}\\p{N}])");

        for (int i = 0; i < word.length(); i++) {
            char current = word.charAt(i);
            if (current == ' ') {
                regex.append("(?:\\s|[\\p{Punct}_]){1,3}");
                continue;
            }

            regex.append(characterGroup(current)).append('+');

            if (hasNextLetter(word, i)) {
                regex.append("[\\p{Punct}_]{0,2}");
            }
        }

        regex.append("(?![\\p{L}\\p{N}])");
        return regex.toString();
    }

    private boolean hasNextLetter(String word, int currentIndex) {
        for (int i = currentIndex + 1; i < word.length(); i++) {
            if (word.charAt(i) != ' ') {
                return true;
            }
        }
        return false;
    }

    private String characterGroup(char current) {
        return switch (current) {
            case 'a' -> "(?:[a4@\u00E0\u00E1\u00E2\u00E4\u00E3\u00E5])";
            case 'b' -> "(?:[b8])";
            case 'c' -> "(?:[c\u00E7])";
            case 'e' -> "(?:[e3\u00E8\u00E9\u00EA\u00EB])";
            case 'g' -> "(?:[g69])";
            case 'i' -> "(?:[i1!|\u00EE\u00EF\u00EC\u00ED])";
            case 'l' -> "(?:[l1!|])";
            case 'o' -> "(?:[o0\u00F2\u00F3\u00F4\u00F6\u00F5])";
            case 's' -> "(?:[s5$])";
            case 't' -> "(?:[t7+])";
            case 'u' -> "(?:[u\u00F9\u00FA\u00FB\u00FC])";
            case 'z' -> "(?:[z2])";
            default -> "(?:" + Pattern.quote(String.valueOf(current)) + ")";
        };
    }

    private record ProfanityRule(String canonical, Pattern pattern) {
    }
}
