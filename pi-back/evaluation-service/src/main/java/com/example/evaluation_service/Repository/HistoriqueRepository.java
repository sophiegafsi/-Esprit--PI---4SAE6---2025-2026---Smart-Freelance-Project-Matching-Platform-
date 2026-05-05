package com.example.evaluation_service.Repository;


import com.example.evaluation_service.Entity.Historique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {


    List<Historique> findByUtilisateur(String utilisateur);


    List<Historique> findByEntite(String entite);
}