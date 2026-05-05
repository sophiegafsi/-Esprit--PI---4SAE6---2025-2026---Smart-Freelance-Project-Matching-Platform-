package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.dto.DuplicateCheckRequestDTO;
import com.example.reclamation.dto.DuplicateCheckResponseDTO;
import com.example.reclamation.entites.Reclamation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DuplicateDetectionService {

    private final ReclamationRepository reclamationRepository;

    private static final Set<String> STOP_WORDS = Set.of(
            "le", "la", "les", "un", "une", "des", "de", "du", "et", "ou",
            "a", "au", "aux", "en", "dans", "avec", "pour", "sur",
            "the", "is", "are", "of", "to", "and", "or", "in", "on", "for"
    );

    public List<DuplicateCheckResponseDTO> findSimilarReclamations(DuplicateCheckRequestDTO request) {
        List<Reclamation> allReclamations = reclamationRepository.findAll();

        String newSujet = request.getSujet() == null ? "" : request.getSujet();
        String newDescription = request.getDescription() == null ? "" : request.getDescription();

        List<DuplicateCheckResponseDTO> results = new ArrayList<>();

        for (Reclamation reclamation : allReclamations) {
            String existingSujet = reclamation.getSujet() == null ? "" : reclamation.getSujet();
            String existingDescription = reclamation.getDescription() == null ? "" : reclamation.getDescription();

            double sujetScore = calculateSimilarity(newSujet, existingSujet);
            double descriptionScore = calculateSimilarity(newDescription, existingDescription);

            double finalScore = (sujetScore * 0.6) + (descriptionScore * 0.4);

            if (finalScore >= 0.35) {
                results.add(new DuplicateCheckResponseDTO(
                        reclamation.getIdReclamation(),
                        reclamation.getSujet(),
                        reclamation.getDescription(),
                        finalScore
                ));
            }
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(DuplicateCheckResponseDTO::getSimilarityScore).reversed())
                .collect(Collectors.toList());
    }

    private double calculateSimilarity(String text1, String text2) {
        Set<String> words1 = extractKeywords(text1);
        Set<String> words2 = extractKeywords(text2);

        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return (double) intersection.size() / union.size();
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(
                        text.toLowerCase()
                                .replaceAll("[^a-zA-Zàâçéèêëîïôûùüÿñæœ0-9 ]", " ")
                                .split("\\s+")
                )
                .filter(word -> !word.isBlank())
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}