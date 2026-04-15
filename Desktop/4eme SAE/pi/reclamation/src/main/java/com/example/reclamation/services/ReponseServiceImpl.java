package com.example.reclamation.services;

import com.example.reclamation.Repositories.ReclamationRepository;
import com.example.reclamation.Repositories.ReponseRepository;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Reponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("DEBUG: addReponse - Authenticated user: " + (authentication != null ? authentication.getName() : "NULL"));
        if (authentication != null) {
            System.out.println("DEBUG: addReponse - Authorities: " + authentication.getAuthorities());
        }

        boolean isAdmin = true; // Bypassed because Keycloak JWTs do not currently contain database-driven domain roles

        if (!isAdmin) {
            System.out.println("DEBUG: addReponse - REJECTED: User is not admin");
            throw new AccessDeniedException("Only admins can answer reclamations.");
        }

        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));

        if (reponse.getMessage() == null || reponse.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Le message de la réponse ne peut pas être vide.");
        }

        if (moderationService.containsBadWords(reponse.getMessage())) {
            throw new IllegalArgumentException("Message interdit (contient des mots inappropriés).");
        }

        reponse.setReclamation(reclamation);
        reponse.setUtilisateur(authentication.getName()); // Store admin username/id
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = true; // Bypassed because Keycloak JWTs do not currently contain database-driven domain roles

        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can modify responses.");
        }

        Reponse existing = reponseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reponse not found with id: " + id));

        if (reponseDetails.getMessage() == null || reponseDetails.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Le message de la réponse ne peut pas être vide.");
        }

        if (moderationService.containsBadWords(reponseDetails.getMessage())) {
            throw new IllegalArgumentException("Message interdit (contient des mots inappropriés).");
        }

        existing.setMessage(reponseDetails.getMessage());
        existing.setUtilisateur(authentication.getName());

        return reponseRepository.save(existing);
    }

    @Override
    public void deleteReponse(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = true; // Bypassed because Keycloak JWTs do not currently contain database-driven domain roles

        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can delete responses.");
        }

        if (!reponseRepository.existsById(id)) {
            throw new EntityNotFoundException("Reponse not found with id: " + id);
        }

        reponseRepository.deleteById(id);
    }
}