package com.example.projet_service.services;

import com.example.projet_service.Repositories.ProjetDetailleRepository;
import com.example.projet_service.Repositories.ProjetRepository;
import com.example.projet_service.dto.ProjectHealthResponse;
import com.example.projet_service.entites.Projet;
import com.example.projet_service.entites.ProjetDetaille;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectHealthServiceTest {

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private ProjetDetailleRepository detailRepo;

    @InjectMocks
    private ProjectHealthService projectHealthService;

    @Test
    @DisplayName("Should return GREEN health for project with on-time tasks")
    void testComputeProjectHealthGreen() {
        // Arrange
        Long projectId = 1L;
        Projet projet = new Projet();
        projet.setId(projectId);
        projet.setTitle("Healthy Project");
        
        ProjetDetaille task = new ProjetDetaille();
        task.setDeadline(LocalDate.now().plusDays(10)); // Far in future
        
        when(projetRepository.findById(projectId)).thenReturn(Optional.of(projet));
        when(detailRepo.findByProjetId(projectId)).thenReturn(Arrays.asList(task));

        // Act
        ProjectHealthResponse response = projectHealthService.computeProjectHealth(projectId);

        // Assert
        assertEquals("GREEN", response.getNiveau());
        assertEquals(100, response.getScore());
    }

    @Test
    @DisplayName("Should return RED health for project with multiple overdue tasks")
    void testComputeProjectHealthRed() {
        // Arrange
        Long projectId = 1L;
        Projet projet = new Projet();
        projet.setId(projectId);
        
        ProjetDetaille overdueTask1 = new ProjetDetaille();
        overdueTask1.setDeadline(LocalDate.now().minusDays(5));
        
        ProjetDetaille overdueTask2 = new ProjetDetaille();
        overdueTask2.setDeadline(LocalDate.now().minusDays(10));

        when(projetRepository.findById(projectId)).thenReturn(Optional.of(projet));
        when(detailRepo.findByProjetId(projectId)).thenReturn(Arrays.asList(overdueTask1, overdueTask2));

        // Act
        ProjectHealthResponse response = projectHealthService.computeProjectHealth(projectId);

        // Assert
        assertEquals("RED", response.getNiveau());
        assertTrue(response.getScore() < 70);
        assertEquals(2, response.getOverdueTasks());
    }

    @Test
    @DisplayName("Should apply penalty for project with no tasks")
    void testComputeProjectHealthNoTasks() {
        // Arrange
        Long projectId = 1L;
        Projet projet = new Projet();
        projet.setId(projectId);

        when(projetRepository.findById(projectId)).thenReturn(Optional.of(projet));
        when(detailRepo.findByProjetId(projectId)).thenReturn(Collections.emptyList());

        // Act
        ProjectHealthResponse response = projectHealthService.computeProjectHealth(projectId);

        // Assert
        // Starting score 100 - 15 (penalty for 0 tasks) = 85 (Still Green but reduced)
        assertEquals(85, response.getScore());
    }
}
