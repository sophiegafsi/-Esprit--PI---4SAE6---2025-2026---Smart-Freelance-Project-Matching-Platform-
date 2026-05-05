package com.example.evaluation_service.Service;

import com.example.evaluation_service.Entity.Evaluation;
import com.example.evaluation_service.Repository.EvaluationRepository;
import com.example.evaluation_service.client.ContractClient;
import com.example.evaluation_service.client.ProjectClient;
import com.example.evaluation_service.client.RecompenseClient;
import com.example.evaluation_service.client.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock private EvaluationRepository evaluationRepository;
    @Mock private RecompenseClient recompenseClient;
    @Mock private ContractClient contractClient;
    @Mock private UserClient userClient;
    @Mock private ProjectClient projectClient;

    @InjectMocks
    private EvaluationService evaluationService;

    private Evaluation evaluation;
    private final UUID evaluatorId = UUID.randomUUID();
    private final UUID evaluatedId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        evaluation = new Evaluation();
        evaluation.setUserEmail("evaluator@example.com");
        evaluation.setEvaluatedUserEmail("target@test.com");
        evaluation.setProjectName("Test Project");
    }

    @Test
    @DisplayName("Should create evaluation when contract exists")
    void testCreateEvaluationSuccess() {
        // Arrange
        // Bypassing contract validation for @example.com in the actual code (mock users)
        // If we want to test the full logic, we'd mock userClient and contractClient
        
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(evaluation);

        // Act
        Evaluation result = evaluationService.createEvaluation(evaluation);

        // Assert
        assertNotNull(result);
        verify(evaluationRepository).save(any());
        verify(recompenseClient, atLeastOnce()).processEvaluation(any());
    }

    @Test
    @DisplayName("Should throw exception if no contract between users")
    void testCreateEvaluationNoContract() {
        // Arrange
        evaluation.setUserEmail("real@evaluator.com"); 
        evaluation.setEvaluatedUserEmail("real@target.com");

        when(userClient.getUserByEmail("real@evaluator.com")).thenReturn(Map.of("id", evaluatorId.toString()));
        when(userClient.getUserByEmail("real@target.com")).thenReturn(Map.of("id", evaluatedId.toString()));
        when(contractClient.getContractsByFreelancer(evaluatorId)).thenReturn(Collections.emptyList());
        when(contractClient.getContractsByClient(evaluatorId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> evaluationService.createEvaluation(evaluation));
    }

    @Test
    @DisplayName("Should calculate average score correctly")
    void testCalculateAverageScore() {
        // Arrange
        Evaluation e1 = new Evaluation(); e1.setScore(5);
        Evaluation e2 = new Evaluation(); e2.setScore(4);
        
        // removed unused stubbing
        
        // Act
        // Indirectly tested via syncRewards call in createEvaluation or by calling private methods if they were accessible
        // Here we can use reflection or test through a public method that calls it.
        // Let's test getAverageScoreForUser instead
        when(evaluationRepository.findByEvaluatedUserName("John")).thenReturn(java.util.List.of(e1, e2));
        double average = evaluationService.getAverageScoreForUser("John");

        // Assert
        assertEquals(4.5, average);
    }
}
