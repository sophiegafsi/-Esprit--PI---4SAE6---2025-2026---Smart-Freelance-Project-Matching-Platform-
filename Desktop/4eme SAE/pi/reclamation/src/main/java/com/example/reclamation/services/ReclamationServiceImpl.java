package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.dto.ReclamationDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReclamationServiceImpl implements IReclamationService {

    private final ReclamationRepository reclamationRepository;

    @Override
    public Reclamation createReclamation(Reclamation reclamation) {
        if (reclamation.getStatut() == null) {
            reclamation.setStatut(Statut.EN_ATTENTE);
        }
        return reclamationRepository.save(reclamation);
    }

    @Override
    public List<ReclamationDTO> getAllReclamations() {
        return reclamationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public ReclamationDTO getReclamationById(Integer id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + id));

        return mapToDTO(reclamation);
    }

    @Override
    @Transactional
    public Reclamation updateReclamation(Integer id, Reclamation reclamationDetails) {
        Reclamation existing = reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + id));

        if (existing.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("Cannot modify reclamation because it is already being processed or resolved.");
        }

        existing.setSujet(reclamationDetails.getSujet());
        existing.setDescription(reclamationDetails.getDescription());
        existing.setPriorite(reclamationDetails.getPriorite());
        existing.setType(reclamationDetails.getType());

        return reclamationRepository.save(existing);
    }

    @Override
    public void deleteReclamation(Integer id) {
        if (!reclamationRepository.existsById(id)) {
            throw new EntityNotFoundException("Reclamation not found with id: " + id);
        }
        reclamationRepository.deleteById(id);
    }

    @Override
    public List<ReclamationDTO> searchReclamations(String search, Type type, Priorite priorite, Statut statut) {
        return reclamationRepository.searchReclamations(search, type, priorite, statut)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private boolean isUrgent(Reclamation reclamation) {
        return reclamation.getPriorite() == Priorite.HAUTE
                || reclamation.getPriorite() == Priorite.CRITIQUE;
    }

    private String getUrgentReason(Reclamation reclamation) {
        if (reclamation.getPriorite() == Priorite.CRITIQUE) {
            return "Priorité critique";
        }

        if (reclamation.getPriorite() == Priorite.HAUTE) {
            return "Priorité élevée";
        }

        return "";
    }

    private ReclamationDTO mapToDTO(Reclamation r) {
        ReclamationDTO dto = new ReclamationDTO();

        dto.setIdReclamation(r.getIdReclamation());
        dto.setSujet(r.getSujet());
        dto.setDescription(r.getDescription());
        dto.setDateCreation(r.getDateCreation());
        dto.setStatut(r.getStatut());
        dto.setPriorite(r.getPriorite());
        dto.setType(r.getType());

        dto.setUrgent(isUrgent(r));
        dto.setUrgentReason(getUrgentReason(r));

        return dto;
    }
}