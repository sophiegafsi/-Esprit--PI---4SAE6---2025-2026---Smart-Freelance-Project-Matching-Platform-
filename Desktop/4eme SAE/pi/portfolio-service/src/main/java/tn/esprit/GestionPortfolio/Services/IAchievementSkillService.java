package tn.esprit.GestionPortfolio.Services;

import tn.esprit.GestionPortfolio.Entities.AchievementSkill;

import java.util.List;

public interface IAchievementSkillService {
    AchievementSkill addAchievementSkill(Long achievementId, AchievementSkill achievementSkill);
    AchievementSkill updateAchievementSkill(AchievementSkill achievementSkill);
    void deleteAchievementSkill(Long id);
    AchievementSkill getAchievementSkillById(Long id);
    List<AchievementSkill> getSkillsByAchievementId(Long achievementId);
}