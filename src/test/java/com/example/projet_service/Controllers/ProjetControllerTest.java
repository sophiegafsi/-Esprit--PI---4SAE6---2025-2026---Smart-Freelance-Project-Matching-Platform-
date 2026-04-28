package com.example.projet_service.Controllers;

import com.example.projet_service.entites.Domaine;
import com.example.projet_service.entites.Projet;
import com.example.projet_service.services.IProjetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjetController.class)
class ProjetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProjetService projetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllProjets() throws Exception {
        Projet projet = new Projet(1L, "Test Projet", "Description", LocalDate.now().plusDays(1), Domaine.WEB, List.of());
        when(projetService.listerTousProjets()).thenReturn(List.of(projet));

        mockMvc.perform(get("/api/projets/allprojets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Projet"));
    }

    @Test
    void shouldCreateProjet() throws Exception {
        Projet projet = new Projet(null, "Nom Test", "Desc", LocalDate.now().plusDays(10), Domaine.MOBILE, List.of());
        Projet savedProjet = new Projet(1L, projet.getTitle(), projet.getDescription(), projet.getDate(), projet.getDomaine(), List.of());

        when(projetService.ajouterProjet(any(Projet.class))).thenReturn(savedProjet);

        mockMvc.perform(post("/api/projets/addprojet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Nom Test"));
    }
}
