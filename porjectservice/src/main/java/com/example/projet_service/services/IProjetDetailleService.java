package com.example.projet_service.services;

import com.example.projet_service.entites.ProjetDetaille;

import java.util.List;
import java.util.Optional;

public interface IProjetDetailleService {
    ProjetDetaille ajouterTache(Long projetId, ProjetDetaille tache);
    List<ProjetDetaille> listerTachesParProjet(Long projetId);
    Optional<ProjetDetaille> voirTache(Long id);
    ProjetDetaille modifierTache(Long id, ProjetDetaille tache);
    void supprimerTache(Long id);
}
