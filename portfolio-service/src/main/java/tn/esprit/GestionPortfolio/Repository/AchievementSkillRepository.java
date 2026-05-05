package tn.esprit.GestionPortfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;

import java.util.Collection;
import java.util.List;

public interface AchievementSkillRepository extends JpaRepository<AchievementSkill, Long> {
    List<AchievementSkill> findByAchievementId(Long achievementId);
    List<AchievementSkill> findByAchievementIdIn(Collection<Long> achievementIds);
}
