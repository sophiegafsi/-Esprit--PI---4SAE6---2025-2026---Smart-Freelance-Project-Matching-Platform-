package com.example.projet_service.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DevisResponse {
    private int prixMinimum;
    private int prixRecommande;
    private int prixMaximum;

    private int heuresEstimees;
    private int tauxHoraire;
    private double facteurComplexite;
    private double facteurUrgence;
    private int joursDisponibles;

    private int confiance;
    private String recommandation;

    private List<PosteDevis> decomposition;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class PosteDevis {
        private String poste;
        private int heures;
        private int montant;
        private int pourcentage;
    }
}