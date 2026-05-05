package com.example.evaluation_service.Controller;

import com.example.evaluation_service.Entity.Evaluation;
import com.example.evaluation_service.Service.EvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EvaluationService evaluationService;

    @Test
    @DisplayName("POST /evaluations/add - Should return created evaluation")
    void testAddEvaluation() throws Exception {
        // Arrange
        Evaluation evaluation = new Evaluation();
        evaluation.setProjectName("Test UI Project");
        
        when(evaluationService.createEvaluation(any())).thenReturn(evaluation);

        // Act & Assert
        mockMvc.perform(post("/evaluations/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evaluation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Test UI Project"));
    }

    @Test
    @DisplayName("GET /evaluations/average/{userName} - Should return numeric average")
    void testGetAverageScore() throws Exception {
        // Arrange
        when(evaluationService.getAverageScoreForUser("John")).thenReturn(4.2);

        // Act & Assert
        mockMvc.perform(get("/evaluations/average/John"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.2"));
    }

    @Test
    @DisplayName("GET /evaluations/eligible-targets/{email} - Should return list of targets")
    void testGetEligibleTargets() throws Exception {
        // Arrange
        when(evaluationService.getEligibleEvaluationTargets("test@email.com"))
                .thenReturn(Collections.singletonList(java.util.Map.of("name", "Alice")));

        // Act & Assert
        mockMvc.perform(get("/evaluations/eligible-targets/test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }
}
