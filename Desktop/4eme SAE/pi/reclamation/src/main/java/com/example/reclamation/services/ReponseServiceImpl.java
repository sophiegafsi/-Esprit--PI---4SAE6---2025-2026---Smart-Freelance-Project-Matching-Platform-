package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.Repositories.ReponseRepository;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Reponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReponseServiceImpl implements IReponseService {

    private final ReponseRepository reponseRepository;
    private final ReclamationRepository reclamationRepository;
    private final ReponseModerationService moderationService;

    @Override
    public Reponse addReponse(Integer reclamationId, Reponse reponse) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));

        if (reponse.getMessage() == null || reponse.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Le message de la réponse ne peut pas être vide.");
        }

        if (moderationService.containsBadWords(reponse.getMessage())) {
            throw new IllegalArgumentException("Message interdit (contient des mots inappropriés).");
        }

        reponse.setReclamation(reclamation);
        return reponseRepository.save(reponse);
    }

    @Override
    public List<Reponse> getReponsesByReclamationId(Integer reclamationId) {
        if (!reclamationRepository.existsById(reclamationId)) {
            throw new EntityNotFoundException("Reclamation not found with id: " + reclamationId);
        }

        return reponseRepository.findByReclamation_IdReclamation(reclamationId);
    }

    @Override
    @Transactional
    public Reponse updateReponse(Integer id, Reponse reponseDetails) {
        Reponse existing = reponseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reponse not found with id: " + id));

        if (reponseDetails.getMessage() == null || reponseDetails.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Le message de la réponse ne peut pas être vide.");
        }

        if (moderationService.containsBadWords(reponseDetails.getMessage())) {
            throw new IllegalArgumentException("Message interdit (contient des mots inappropriés).");
        }

        existing.setMessage(reponseDetails.getMessage());
        existing.setUtilisateur(reponseDetails.getUtilisateur());

        return reponseRepository.save(existing);
    }

    @Override
    public void deleteReponse(Integer id) {
        if (!reponseRepository.existsById(id)) {
            throw new EntityNotFoundException("Reponse not found with id: " + id);
        }

        reponseRepository.deleteById(id);
    }
}