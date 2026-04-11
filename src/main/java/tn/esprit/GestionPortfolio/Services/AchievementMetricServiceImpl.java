package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Repository.AchievementMetricRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;

@Service
@RequiredArgsConstructor
public class AchievementMetricServiceImpl implements IAchievementMetricService {

    private final AchievementMetricRepository achievementMetricRepository;
    private final AchievementRepository achievementRepository;

    @Override
    public AchievementMetric addAchievementMetric(Long achievementId, AchievementMetric achievementMetric) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement != null) {
            achievementMetric.setAchievement(achievement);
            return achievementMetricRepository.save(achievementMetric);
        }
        return null;
    }

    @Override
    public AchievementMetric updateAchievementMetric(AchievementMetric achievementMetric) {
        return achievementMetricRepository.save(achievementMetric);
    }

    @Override
    @Transactional
    public void deleteAchievementMetric(Long id) {
        achievementMetricRepository.findById(id).ifPresent(metric -> {
            Achievement achievement = metric.getAchievement();
            if (achievement != null && achievement.getAchievementMetric() != null) {
                achievement.setAchievementMetric(null);
            }
            metric.setAchievement(null);
            achievementMetricRepository.delete(metric);
        });
    }

    @Override
    public AchievementMetric getMetricByAchievementId(Long achievementId) {
        return achievementMetricRepository.findByAchievementId(achievementId).orElse(null);
    }
}
