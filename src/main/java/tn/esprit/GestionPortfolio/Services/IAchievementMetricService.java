package tn.esprit.GestionPortfolio.Services;

import tn.esprit.GestionPortfolio.Entities.AchievementMetric;

public interface IAchievementMetricService {
    AchievementMetric addAchievementMetric(Long achievementId, AchievementMetric achievementMetric);
    AchievementMetric updateAchievementMetric(AchievementMetric achievementMetric);
    void deleteAchievementMetric(Long id);
    AchievementMetric getMetricByAchievementId(Long achievementId);
}