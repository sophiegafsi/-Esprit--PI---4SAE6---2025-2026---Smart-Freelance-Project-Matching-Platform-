package com.example.projet_service.Repositories;

import com.example.projet_service.entites.ProjetDetaille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjetDetailleRepository extends JpaRepository<ProjetDetaille, Long> {
    List<ProjetDetaille> findByProjetId(Long projetId);
    List<ProjetDetaille> findByTasknameContainingIgnoreCaseAndProjetId(String taskname, Long projetId);
}
