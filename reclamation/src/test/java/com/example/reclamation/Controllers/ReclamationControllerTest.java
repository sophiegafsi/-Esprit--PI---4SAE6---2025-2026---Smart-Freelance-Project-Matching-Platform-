package com.example.reclamation.Controllers;

import com.example.reclamation.dto.DuplicateCheckResponseDTO;
import com.example.reclamation.dto.ReclamationDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import com.example.reclamation.services.DuplicateDetectionService;
import com.example.reclamation.services.IReclamationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReclamationController.class)
class ReclamationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IReclamationService reclamationService;

    @MockBean
    private DuplicateDetectionService duplicateDetectionService;

    @Test
    void shouldReturnAllReclamations() throws Exception {
        ReclamationDTO dto = new ReclamationDTO();
        dto.setIdReclamation(1);
        dto.setSujet("Test");
        dto.setDescription("Description");
        dto.setDateCreation(new Date());
        dto.setPriorite(Priorite.BASSE);
        dto.setStatut(Statut.EN_ATTENTE);
        dto.setType(Type.PROJET);

        when(reclamationService.getAllReclamations()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reclamations/list").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReclamation").value(1))
                .andExpect(jsonPath("$[0].sujet").value("Test"));
    }

    @Test
    void shouldCreateReclamation() throws Exception {
        Reclamation reclamation = new Reclamation();
        reclamation.setSujet("New");
        reclamation.setDescription("New description");
        reclamation.setPriorite(Priorite.BASSE);
        reclamation.setStatut(Statut.EN_ATTENTE);
        reclamation.setType(Type.PROJET);

        when(reclamationService.createReclamation(any(Reclamation.class))).thenReturn(reclamation);

        mockMvc.perform(post("/api/reclamations/addreclamation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reclamation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sujet").value("New"));
    }

    @Test
    void shouldSearchReclamations() throws Exception {
        ReclamationDTO dto = new ReclamationDTO();
        dto.setIdReclamation(1);
        dto.setSujet("Search");
        dto.setDescription("Search description");
        dto.setDateCreation(new Date());
        dto.setPriorite(Priorite.BASSE);
        dto.setStatut(Statut.EN_ATTENTE);
        dto.setType(Type.PROJET);

        when(reclamationService.searchReclamations("term", Type.PROJET, Priorite.BASSE, Statut.EN_ATTENTE))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reclamations/search")
                        .param("search", "term")
                        .param("type", "PROJET")
                        .param("priorite", "BASSE")
                        .param("statut", "EN_ATTENTE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sujet").value("Search"));
    }

    @Test
    void shouldCheckDuplicates() throws Exception {
        DuplicateCheckResponseDTO response = new DuplicateCheckResponseDTO(1, "Test", "Desc", 0.9);
        when(duplicateDetectionService.findSimilarReclamations(any())).thenReturn(List.of(response));

        String payload = objectMapper.writeValueAsString(new com.example.reclamation.dto.DuplicateCheckRequestDTO("Test", "Desc"));

        mockMvc.perform(post("/api/reclamations/check-duplicates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].similarityScore").value(0.9));
    }
}
