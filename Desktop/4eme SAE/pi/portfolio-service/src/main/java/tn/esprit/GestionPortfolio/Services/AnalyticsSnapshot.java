package tn.esprit.GestionPortfolio.Services;

import tn.esprit.GestionPortfolio.DTO.SkillDTO;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;

import java.util.List;
import java.util.Map;

// Regroupe toutes les données utiles à l'analyse pour éviter
// de relancer des accès BD dans plusieurs services métier.
public record AnalyticsSnapshot(
        List<Achievement> achievements,
        List<AchievementSkill> achievementSkills,
        Map<Long, AchievementMetric> metricsByAchievementId,
        Map<Long, SkillDTO> skillsById
) {
    public static AnalyticsSnapshot empty() {
        return new AnalyticsSnapshot(List.of(), List.of(), Map.of(), Map.of());
    }
}
