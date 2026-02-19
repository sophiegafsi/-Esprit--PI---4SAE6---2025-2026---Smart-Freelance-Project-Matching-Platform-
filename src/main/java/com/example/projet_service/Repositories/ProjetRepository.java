package com.example.projet_service.Repositories;
import com.example.projet_service.entites.Domaine;
import com.example.projet_service.entites.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    List<Projet> findByDomaine(Domaine domaine);
    List<Projet> findByTitleContainingIgnoreCase(String title);
}
