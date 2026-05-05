package com.example.reclamation.services;

import com.example.reclamation.dto.ReclamationDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;

import java.util.List;

public interface IReclamationService {

    Reclamation createReclamation(Reclamation reclamation);

    List<ReclamationDTO> getAllReclamations();

    ReclamationDTO getReclamationById(Integer id);

    Reclamation updateReclamation(Integer id, Reclamation reclamationDetails);

    void deleteReclamation(Integer id);

    List<ReclamationDTO> searchReclamations(
            String search,
            Type type,
            Priorite priorite,
            Statut statut
    );
}