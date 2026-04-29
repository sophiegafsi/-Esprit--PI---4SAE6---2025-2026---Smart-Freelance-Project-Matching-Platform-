package com.example.reclamation.Controllers;

import com.example.reclamation.entites.Reponse;
import com.example.reclamation.services.IReponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReponseController.class)
class ReponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IReponseService reponseService;

    @Test
    void shouldAddReponse() throws Exception {
        Reponse reponse = new Reponse();
        reponse.setMessage("Hello");
        reponse.setUtilisateur("user1");

        when(reponseService.addReponse(anyInt(), any(Reponse.class))).thenReturn(reponse);

        mockMvc.perform(post("/api/reclamations/1/reponses/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reponse)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Hello"))
                .andExpect(jsonPath("$.utilisateur").value("user1"));
    }

    @Test
    void shouldGetReponsesByReclamation() throws Exception {
        Reponse response1 = new Reponse();
        response1.setIdReponse(1);
        response1.setMessage("Answer");
        response1.setUtilisateur("user1");

        when(reponseService.getReponsesByReclamationId(1)).thenReturn(List.of(response1));

        mockMvc.perform(get("/api/reclamations/1/reponses/list").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReponse").value(1))
                .andExpect(jsonPath("$[0].message").value("Answer"));
    }

    @Test
    void shouldUpdateReponse() throws Exception {
        Reponse reponse = new Reponse();
        reponse.setIdReponse(1);
        reponse.setMessage("Updated");
        reponse.setUtilisateur("user1");

        when(reponseService.updateReponse(anyInt(), any(Reponse.class))).thenReturn(reponse);

        mockMvc.perform(put("/api/reclamations/1/reponses/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated"));
    }

    @Test
    void shouldDeleteReponse() throws Exception {
        doNothing().when(reponseService).deleteReponse(1);

        mockMvc.perform(delete("/api/reclamations/1/reponses/delete/1"))
                .andExpect(status().isNoContent());
    }
}
