// ReviewRepository.java
package com.example.evaluation_service.Repository;

import com.example.evaluation_service.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ Méthode existante
    List<Review> findByEvaluation_Id(Long id);

    // ✅ NOUVELLE MÉTHODE: Trouver par email utilisateur
    List<Review> findByUserEmail(String userEmail);

    // ✅ NOUVELLE MÉTHODE: Compter par email utilisateur
    long countByUserEmail(String userEmail);

    // ✅ NOUVELLE MÉTHODE: Vérifier l'existence
    boolean existsByUserEmail(String userEmail);

    // ✅ NOUVELLE MÉTHODE: Supprimer par email utilisateur
    void deleteByUserEmail(String userEmail);
}