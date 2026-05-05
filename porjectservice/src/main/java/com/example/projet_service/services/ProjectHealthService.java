package com.example.projet_service.services;

import com.example.projet_service.Repositories.ProjetDetailleRepository;
import com.example.projet_service.Repositories.ProjetRepository;
import com.example.projet_service.dto.ProjectHealthResponse;
import com.example.projet_service.entites.Projet;
import com.example.projet_service.entites.ProjetDetaille;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ProjectHealthService {

    private final ProjetRepository projetRepository;
    private final ProjetDetailleRepository detailRepo;

    public ProjectHealthService(ProjetRepository projetRepository, ProjetDetailleRepository detailRepo) {
        this.projetRepository = projetRepository;
        this.detailRepo = detailRepo;
    }

    public ProjectHealthResponse computeProjectHealth(Long projetId) {
        // Charger le projet
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + projetId));

        // Charger les tâches
        List<ProjetDetaille> tasks = detailRepo.findByProjetId(projetId);

        LocalDate today = LocalDate.now();

        int total = tasks.size();
        int overdue = 0;
        int urgent = 0;
        int soon = 0;

        for (ProjetDetaille t : tasks) {
            if (t.getDeadline() == null) continue;

            long diff = ChronoUnit.DAYS.between(today, t.getDeadline());

            if (diff < 0) overdue++;
            else if (diff <= 2) urgent++;
            else if (diff <= 7) soon++;
        }

        // ===== Score () =====
        int score = 100;

        // Beaucoup de retard = gros impact
        score -= overdue * 20;

        // Urgent = risque moyen
        score -= urgent * 10;

        // Beaucoup de tâches proches deadline => pression
        if (total > 0 && (soon + urgent + overdue) >= Math.ceil(total * 0.5)) {
            score -= 10;
        }

        // Projet sans tâches = projet non structuré
        if (total == 0) {
            score -= 15;
        }

        // Clamp 0..100
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        String niveau;
        String message;

        if (score >= 80) {
            niveau = "GREEN";
            message = "Projet en bonne santé ✅";
        } else if (score >= 50) {
            niveau = "YELLOW";
            message = "Attention : risque modéré ⚠️";
        } else {
            niveau = "RED";
            message = "Projet en danger 🔥";
        }

        int joursDeadlineProjet = 0;
        if (projet.getDate() != null) {
            joursDeadlineProjet = (int) ChronoUnit.DAYS.between(today, projet.getDate());
        }

        return new ProjectHealthResponse(
                projet.getId(),
                projet.getTitle(),
                score,
                niveau,
                message,
                total,
                overdue,
                urgent,
                soon,
                joursDeadlineProjet
        );
    }

    public List<ProjectHealthResponse> computeAllProjectsHealth() {
        return projetRepository.findAll().stream()
                .map(p -> computeProjectHealth(p.getId()))
                .toList();
    }
}