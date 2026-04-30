package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.dto.ReclamationDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamationServiceImplTest {

    @Mock
    private ReclamationRepository reclamationRepository;

    @InjectMocks
    private ReclamationServiceImpl reclamationService;

    @Test
    void createReclamation_shouldSetDefaultStatusWhenNull() {
        Reclamation reclamation = new Reclamation();
        reclamation.setSujet("Test");
        reclamation.setDescription("Desc");
        reclamation.setPriorite(Priorite.HAUTE);
        reclamation.setType(Type.TECHNIQUE);

        when(reclamationRepository.save(any(Reclamation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reclamation result = reclamationService.createReclamation(reclamation);

        assertEquals(Statut.EN_ATTENTE, result.getStatut());
        verify(reclamationRepository).save(reclamation);
    }

    @Test
    void getReclamationById_shouldReturnDTO() {
        Reclamation r = new Reclamation();
        r.setIdReclamation(1);
        r.setSujet("Bug");
        r.setDescription("Erreur");
        r.setPriorite(Priorite.CRITIQUE);
        r.setType(Type.TECHNIQUE);
        r.setStatut(Statut.EN_ATTENTE);

        when(reclamationRepository.findById(1)).thenReturn(Optional.of(r));

        ReclamationDTO dto = reclamationService.getReclamationById(1);

        assertEquals(1, dto.getIdReclamation());
        assertTrue(dto.isUrgent());
        assertEquals("Priorité critique", dto.getUrgentReason());
    }

    @Test
    void getReclamationById_shouldThrowException() {
        when(reclamationRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> reclamationService.getReclamationById(99));
    }

    @Test
    void updateReclamation_shouldWork() {
        Reclamation existing = new Reclamation();
        existing.setIdReclamation(1);
        existing.setStatut(Statut.EN_ATTENTE);

        Reclamation details = new Reclamation();
        details.setSujet("New");
        details.setDescription("New desc");
        details.setPriorite(Priorite.MOYENNE);
        details.setType(Type.PROJET);

        when(reclamationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(reclamationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reclamation result = reclamationService.updateReclamation(1, details);

        assertEquals("New", result.getSujet());
        assertEquals(Priorite.MOYENNE, result.getPriorite());
    }

    @Test
    void updateReclamation_shouldFailIfNotPending() {
        Reclamation existing = new Reclamation();
        existing.setIdReclamation(1);
        existing.setStatut(Statut.RESOLUE);

        when(reclamationRepository.findById(1)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> reclamationService.updateReclamation(1, new Reclamation()));
    }

    @Test
    void deleteReclamation_shouldWork() {
        when(reclamationRepository.existsById(1)).thenReturn(true);

        reclamationService.deleteReclamation(1);

        verify(reclamationRepository).deleteById(1);
    }

    @Test
    void deleteReclamation_shouldThrow() {
        when(reclamationRepository.existsById(99)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> reclamationService.deleteReclamation(99));
    }

    @Test
    void getAllReclamations_shouldReturnList() {
        Reclamation r = new Reclamation();
        r.setIdReclamation(1);
        r.setSujet("Test");
        r.setDescription("Desc");
        r.setPriorite(Priorite.HAUTE);
        r.setType(Type.PAIEMENT);
        r.setStatut(Statut.EN_ATTENTE);

        when(reclamationRepository.findAll()).thenReturn(List.of(r));

        List<ReclamationDTO> result = reclamationService.getAllReclamations();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isUrgent());
    }
}
