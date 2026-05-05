package com.example.projet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProjectHealthResponse {

    private Long projetId;
    private String projetTitle;

    private int score;           // 0..100
    private String niveau;       // GREEN / YELLOW / RED
    private String message;      // message lisible

    private int totalTasks;
    private int overdueTasks;
    private int urgentTasks;
    private int soonTasks;       // 3..7j par ex

    private int joursJusquaDeadlineProjet; // basé sur projet.date (optionnel)
}