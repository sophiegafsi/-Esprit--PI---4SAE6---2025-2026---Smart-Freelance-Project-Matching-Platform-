package com.example.reclamation.Repositories;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Integer> {

    @Query("""
        SELECT r FROM Reclamation r
        WHERE
            (:search IS NULL OR :search = '' OR
             LOWER(r.sujet) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:type IS NULL OR r.type = :type)
        AND (:priorite IS NULL OR r.priorite = :priorite)
        AND (:statut IS NULL OR r.statut = :statut)
        ORDER BY r.dateCreation DESC
    """)
        List<Reclamation> searchReclamations(
                @Param("search") String search,
                @Param("type") Type type,
                @Param("priorite") Priorite priorite,
                @Param("statut") Statut statut
        );
    List<Reclamation> findAll();

    }

