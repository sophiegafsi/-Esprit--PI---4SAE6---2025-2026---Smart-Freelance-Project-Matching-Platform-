package com.example.projet_service.Controllers;

import com.example.projet_service.dto.ProjectHealthResponse;
import com.example.projet_service.services.ProjectHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")

public class ProjectHealthController {

    private final ProjectHealthService healthService;

    public ProjectHealthController(ProjectHealthService healthService) {
        this.healthService = healthService;
    }

    // Santé d’un projet
    @GetMapping("/project/{projetId}")
    public ResponseEntity<ProjectHealthResponse> getProjectHealth(@PathVariable Long projetId) {
        return ResponseEntity.ok(healthService.computeProjectHealth(projetId));
    }

    // Santé de tous les projets (utile pour liste projets)
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectHealthResponse>> getAllProjectsHealth() {
        return ResponseEntity.ok(healthService.computeAllProjectsHealth());
    }
}