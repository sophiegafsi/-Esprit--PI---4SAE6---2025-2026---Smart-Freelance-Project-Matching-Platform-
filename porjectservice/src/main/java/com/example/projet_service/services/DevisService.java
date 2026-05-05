package com.example.projet_service.services;

import com.example.projet_service.Repositories.ProjetRepository;
import com.example.projet_service.dto.DevisResponse;
import com.example.projet_service.entites.Projet;
import com.example.projet_service.entites.ProjetDetaille;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DevisService {

    private final ProjetRepository projetRepository;

    // Taux de base (tu peux adapter)
    private final Map<String, Integer> tauxHoraires = Map.ofEntries(
            Map.entry("WEB", 50),
            Map.entry("MOBILE", 60),
            Map.entry("DATA_SCIENCE", 80),
            Map.entry("IA", 100),
            Map.entry("DEVOPS", 70),
            Map.entry("CYBERSECURITY", 90),
            Map.entry("CLOUD_COMPUTING", 75),
            Map.entry("GAME_DEV", 55),
            Map.entry("IOT", 65),
            Map.entry("BIG_DATA", 85),
            Map.entry("BLOCKCHAIN", 95)
    );

    // Mots clés de complexité
    private final Map<String, Double> complexiteMotsCles = Map.ofEntries(
            Map.entry("simple", 0.7),
            Map.entry("facile", 0.7),
            Map.entry("complexe", 1.5),
            Map.entry("difficile", 1.8),
            Map.entry("avancé", 2.0),
            Map.entry("expert", 2.5),
            Map.entry("api", 1.2),
            Map.entry("base de données", 1.3),
            Map.entry("authentification", 1.2),
            Map.entry("paiement", 1.6),
            Map.entry("temps réel", 2.2),
            Map.entry("machine learning", 2.8),
            Map.entry("ia", 3.0),
            Map.entry("sécurité", 1.7)
    );

    public DevisService(ProjetRepository projetRepository) {
        this.projetRepository = projetRepository;
    }

    public DevisResponse calculerDevis(Long projetId, String deadlineOverride) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable: " + projetId));

        String domaine = (projet.getDomaine() != null) ? projet.getDomaine().name() : "WEB";

        // Deadline : on prend override sinon projet.date sinon dans 30 jours
        LocalDate deadline = null;
        if (deadlineOverride != null && !deadlineOverride.isBlank()) {
            deadline = LocalDate.parse(deadlineOverride);
        } else if (projet.getDate() != null) {
            deadline = projet.getDate();
        } else {
            deadline = LocalDate.now().plusDays(30);
        }

        int taux = tauxHoraires.getOrDefault(domaine, 50);

        int nbTasks = (projet.getDetails() != null) ? projet.getDetails().size() : 0;

        double complexite = calculerComplexiteProjet(projet);
        double heures = estimerHeuresProjet(projet, nbTasks);

        int jours = (int) Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), deadline));
        double urgence = calculerFacteurUrgence(jours, heures);

        double prixFinal = heures * taux * complexite * urgence;

        List<DevisResponse.PosteDevis> decomposition = decomposition((int)Math.round(heures), taux, complexite);

        int confiance = calculerConfiance(projet, nbTasks);
        String reco = genererRecommandation(prixFinal);

        return new DevisResponse(
                (int)Math.round(prixFinal * 0.85),
                (int)Math.round(prixFinal),
                (int)Math.round(prixFinal * 1.3),
                (int)Math.round(heures),
                taux,
                round2(complexite),
                round2(urgence),
                jours,
                confiance,
                reco,
                decomposition
        );
    }

    private double estimerHeuresProjet(Projet projet, int nbTasks) {
        // Base
        double heures = 20;

        // Longueur du texte (titre + description)
        String text = ((projet.getTitle() == null ? "" : projet.getTitle()) + " " +
                (projet.getDescription() == null ? "" : projet.getDescription()));
        int motsSignificatifs = (int) Arrays.stream(text.split("\\s+"))
                .filter(w -> w.length() > 3)
                .count();
        heures += motsSignificatifs * 2;

        // Plus il y a de tâches, plus c’est gros
        if (nbTasks > 0) {
            heures += nbTasks * 6;
        }

        // Ajout selon contenu des tâches
        if (projet.getDetails() != null) {
            for (ProjetDetaille t : projet.getDetails()) {
                String td = (t.getTaskname() == null ? "" : t.getTaskname()) + " " +
                        (t.getDescription() == null ? "" : t.getDescription());
                if (td.toLowerCase().contains("api")) heures += 6;
                if (td.toLowerCase().contains("auth")) heures += 5;
                if (td.toLowerCase().contains("paiement")) heures += 10;
            }
        }

        return Math.min(500, Math.max(10, heures));
    }

    private double calculerComplexiteProjet(Projet projet) {
        String text = ((projet.getTitle() == null ? "" : projet.getTitle()) + " " +
                (projet.getDescription() == null ? "" : projet.getDescription())).toLowerCase();

        // Ajouter aussi les tâches dans le texte
        if (projet.getDetails() != null) {
            for (ProjetDetaille t : projet.getDetails()) {
                text += " " + (t.getTaskname() == null ? "" : t.getTaskname()).toLowerCase();
                text += " " + (t.getDescription() == null ? "" : t.getDescription()).toLowerCase();
            }
        }

        double c = 1.0;
        for (Map.Entry<String, Double> e : complexiteMotsCles.entrySet()) {
            if (text.contains(e.getKey())) c *= e.getValue();
        }
        return Math.min(3.0, Math.max(0.5, c));
    }

    private double calculerFacteurUrgence(int jours, double heures) {
        double hParJour = heures / jours;
        if (hParJour <= 4) return 1.0;
        if (hParJour <= 6) return 1.2;
        if (hParJour <= 8) return 1.5;
        return 2.0;
    }

    private List<DevisResponse.PosteDevis> decomposition(int heures, int taux, double complexite) {
        return List.of(
                poste("Développement", (int)Math.round(heures * 0.6), taux, complexite, 60),
                poste("Tests & QA", (int)Math.round(heures * 0.2), taux, complexite, 20),
                poste("Déploiement", (int)Math.round(heures * 0.1), taux, complexite, 10),
                poste("Gestion de projet", (int)Math.round(heures * 0.1), taux, complexite, 10)
        );
    }

    private DevisResponse.PosteDevis poste(String nom, int heures, int taux, double complexite, int p) {
        int montant = (int)Math.round(heures * taux * complexite);
        return new DevisResponse.PosteDevis(nom, heures, montant, p);
    }

    private int calculerConfiance(Projet projet, int nbTasks) {
        int confiance = 60;
        int len = (projet.getDescription() == null) ? 0 : projet.getDescription().length();
        confiance += Math.min(20, len / 50);
        confiance += (projet.getDomaine() != null) ? 10 : 0;
        confiance += Math.min(10, nbTasks); // plus de tâches => plus de précision
        return Math.min(95, Math.max(50, confiance));
    }

    private String genererRecommandation(double prix) {
        if (prix < 500) return "Petit projet - Idéal pour débuter";
        if (prix < 2000) return "Projet de taille moyenne - Budget standard";
        if (prix < 5000) return "Gros projet - Prévoir plusieurs semaines";
        return "Très gros projet - Découper en phases";
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}