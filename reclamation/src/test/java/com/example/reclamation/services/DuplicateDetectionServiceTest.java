package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.dto.DuplicateCheckRequestDTO;
import com.example.reclamation.dto.DuplicateCheckResponseDTO;
import com.example.reclamation.entites.Reclamation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateDetectionServiceTest {

    @Mock
    private ReclamationRepository reclamationRepository;

    @InjectMocks
    private DuplicateDetectionService duplicateDetectionService;

    @Test
    @DisplayName("Should detect very similar reclamations")
    void testFindSimilarReclamations() {
        // Arrange
        Reclamation existing = new Reclamation();
        existing.setIdReclamation(1);
        existing.setSujet("Problème de paiement");
        existing.setDescription("Je n'ai pas reçu mon virement pour le projet X.");

        when(reclamationRepository.findAll()).thenReturn(Arrays.asList(existing));

        DuplicateCheckRequestDTO request = new DuplicateCheckRequestDTO();
        request.setSujet("Problème de paiement");
        request.setDescription("Je n'ai pas reçu mon virement pour le projet X.");

        // Act
        List<DuplicateCheckResponseDTO> results = duplicateDetectionService.findSimilarReclamations(request);

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getSimilarityScore() > 0.4);
        assertEquals("Problème de paiement", results.get(0).getSujet());
    }

    @Test
    @DisplayName("Should not detect duplicates for different topics")
    void testNoDuplicatesForDifferentTopics() {
        // Arrange
        Reclamation existing = new Reclamation();
        existing.setIdReclamation(1);
        existing.setSujet("Bug d'affichage");
        existing.setDescription("La page de profil est vide.");

        when(reclamationRepository.findAll()).thenReturn(Arrays.asList(existing));

        DuplicateCheckRequestDTO request = new DuplicateCheckRequestDTO();
        request.setSujet("Changement de mot de passe");
        request.setDescription("Je veux réinitialiser mon accès.");

        // Act
        List<DuplicateCheckResponseDTO> results = duplicateDetectionService.findSimilarReclamations(request);

        // Assert
        assertTrue(results.isEmpty());
    }
}
