package com.example.projet_service.services;

import com.example.projet_service.entites.Projet;

import java.util.List;
import java.util.Optional;

public interface IProjetService {
    Projet ajouterProjet(Projet projet);
    List<Projet> listerTousProjets();
    Optional<Projet> voirProjet(Long id);
    List<Projet> listerProjetsParClient(java.util.UUID clientId);
    Projet modifierProjet(Long id, Projet projet);
    void supprimerProjet(Long id);
}
