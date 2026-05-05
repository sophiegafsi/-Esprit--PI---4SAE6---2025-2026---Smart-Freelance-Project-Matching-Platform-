package com.example.reclamation.services;
import com.example.reclamation.entites.Reponse;

import java.util.List;

public interface IReponseService {
    Reponse addReponse(Integer reclamationId, Reponse reponse);
    List<Reponse> getReponsesByReclamationId(Integer reclamationId);
    Reponse updateReponse(Integer id, Reponse reponseDetails);
    void deleteReponse(Integer id);
}
