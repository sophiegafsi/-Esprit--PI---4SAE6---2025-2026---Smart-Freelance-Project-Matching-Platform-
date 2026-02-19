package com.example.projet_service.services;

import com.example.projet_service.Repositories.ProjetRepository;
import com.example.projet_service.entites.Projet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjetService implements IProjetService {

    @Autowired
    private ProjetRepository projetRepository;

    @Override
    public Projet ajouterProjet(Projet projet) {
        return projetRepository.save(projet);
    }

    @Override
    public List<Projet> listerTousProjets() {
        return projetRepository.findAll();
    }

    @Override
    public Optional<Projet> voirProjet(Long id) {
        return projetRepository.findById(id);
    }

    @Override
    public Projet modifierProjet(Long id, Projet projetDetails) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));

        projet.setTitle(projetDetails.getTitle());
        projet.setDescription(projetDetails.getDescription());
        projet.setDate(projetDetails.getDate());
        projet.setDomaine(projetDetails.getDomaine());

        return projetRepository.save(projet);
    }

    @Override
    public void supprimerProjet(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));
        projetRepository.delete(projet);
    }
}