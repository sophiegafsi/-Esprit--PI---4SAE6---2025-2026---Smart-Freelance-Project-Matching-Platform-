package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.dto.DuplicateCheckRequestDTO;
import com.example.reclamation.dto.DuplicateCheckResponseDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DuplicateDetectionServiceTest {

    private ReclamationRepository reclamationRepository;
    private DuplicateDetectionService duplicateDetectionService;

    @BeforeEach
    void setUp() {
        reclamationRepository = Mockito.mock(ReclamationRepository.class);
        duplicateDetectionService = new DuplicateDetectionService(reclamationRepository);
    }

    @Test
    void shouldReturnSimilarReclamationWhenSubjectsOverlap() {
        Reclamation existing = new Reclamation();
        existing.setIdReclamation(1);
        existing.setSujet("Problème de paiement");
        existing.setDescription("Le paiement n'est pas passé.");
        existing.setPriorite(Priorite.BASSE);
        existing.setStatut(Statut.EN_ATTENTE);
        existing.setType(Type.PAIEMENT);

        when(reclamationRepository.findAll()).thenReturn(List.of(existing));

        DuplicateCheckRequestDTO request = new DuplicateCheckRequestDTO("Paiement échoué", "Le paiement ne passe pas.");
        List<DuplicateCheckResponseDTO> results = duplicateDetectionService.findSimilarReclamations(request);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getIdReclamation()).isEqualTo(1);
        assertThat(results.get(0).getSimilarityScore()).isGreaterThan(0.0);
    }

    @Test
    void shouldReturnEmptyWhenNoSimilarReclamation() {
        Reclamation existing = new Reclamation();
        existing.setIdReclamation(1);
        existing.setSujet("Site indisponible");
        existing.setDescription("La page d'accueil ne s'affiche pas.");
        existing.setPriorite(Priorite.HAUTE);
        existing.setStatut(Statut.EN_COURS);
        existing.setType(Type.TECHNIQUE);

        when(reclamationRepository.findAll()).thenReturn(List.of(existing));

        DuplicateCheckRequestDTO request = new DuplicateCheckRequestDTO("Question de facturation", "Je veux une facture.");
        List<DuplicateCheckResponseDTO> results = duplicateDetectionService.findSimilarReclamations(request);

        assertThat(results).isEmpty();
    }
}
