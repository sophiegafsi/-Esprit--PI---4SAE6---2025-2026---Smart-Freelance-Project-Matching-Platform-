package tn.esprit.GestionPortfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AchievementMetricRepository extends JpaRepository<AchievementMetric, Long> {
    Optional<AchievementMetric> findByAchievementId(Long achievementId);
    List<AchievementMetric> findByAchievementIdIn(Collection<Long> achievementIds);
}
