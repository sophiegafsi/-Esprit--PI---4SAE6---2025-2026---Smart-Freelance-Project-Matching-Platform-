package com.example.reclamation.Repositories;
import com.example.reclamation.entites.Reponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReponseRepository extends JpaRepository<Reponse, Integer> {
    List<Reponse> findByReclamation_IdReclamation(Integer reclamationId);
}