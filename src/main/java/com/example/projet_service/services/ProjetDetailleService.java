package com.example.projet_service.services;

import com.example.projet_service.Repositories.ProjetDetailleRepository;
import com.example.projet_service.Repositories.ProjetRepository;
import com.example.projet_service.entites.Projet;
import com.example.projet_service.entites.ProjetDetaille;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjetDetailleService implements IProjetDetailleService {

    @Autowired
    private ProjetDetailleRepository projetDetailleRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Override
    public ProjetDetaille ajouterTache(Long projetId, ProjetDetaille tache) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + projetId));

        tache.setProjet(projet);
        return projetDetailleRepository.save(tache);
    }

    @Override
    public List<ProjetDetaille> listerTachesParProjet(Long projetId) {
        return projetDetailleRepository.findByProjetId(projetId);
    }

    @Override
    public Optional<ProjetDetaille> voirTache(Long id) {
        return projetDetailleRepository.findById(id);
    }

    @Override
    public ProjetDetaille modifierTache(Long id, ProjetDetaille tacheDetails) {
        ProjetDetaille tache = projetDetailleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + id));

        tache.setTaskname(tacheDetails.getTaskname());
        tache.setDescription(tacheDetails.getDescription());
        tache.setDeadline(tacheDetails.getDeadline());

        return projetDetailleRepository.save(tache);
    }

    @Override
    public void supprimerTache(Long id) {
        ProjetDetaille tache = projetDetailleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + id));
        projetDetailleRepository.delete(tache);
    }
}
