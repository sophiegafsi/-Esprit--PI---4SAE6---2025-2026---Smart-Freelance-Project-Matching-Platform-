package tn.esprit.GestionPortfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.GestionPortfolio.Entities.Achievement;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByFreelancerId(Long freelancerId);
}