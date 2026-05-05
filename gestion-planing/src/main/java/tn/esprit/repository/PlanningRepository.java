package tn.esprit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.entities.Planning;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long> {
    List<Planning> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description
    );

    List<Planning> findByUserId(String userId);

    List<Planning> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndDescriptionContainingIgnoreCase(
            String userId1, String title,
            String userId2, String description
    );

    Optional<Planning> findByIdAndUserId(Long id, String userId);
}