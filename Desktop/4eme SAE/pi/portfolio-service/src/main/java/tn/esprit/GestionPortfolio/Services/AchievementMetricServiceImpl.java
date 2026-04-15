package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.DTO.AchievementMetricSuggestionResponse;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Repository.AchievementMetricRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;

@Service
@RequiredArgsConstructor
public class AchievementMetricServiceImpl implements IAchievementMetricService {

    private final AchievementMetricRepository achievementMetricRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementMetricScoringService achievementMetricScoringService;

    @Override
    public AchievementMetric addAchievementMetric(Long achievementId, AchievementMetric achievementMetric) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement != null) {
            AchievementMetricSuggestionResponse suggestion = achievementMetricScoringService.buildSuggestion(achievementId);
            achievementMetric.setAchievement(achievement);
            achievementMetric.setComplexityScore(suggestion.complexityScore());
            achievementMetric.setImpactScore(suggestion.impactScore());
            return achievementMetricRepository.save(achievementMetric);
        }
        return null;
    }

    @Override
    public AchievementMetric updateAchievementMetric(AchievementMetric achievementMetric) {
        Long achievementId = achievementMetric.getAchievement() == null ? null : achievementMetric.getAchievement().getId();
        if (achievementId != null) {
            AchievementMetricSuggestionResponse suggestion = achievementMetricScoringService.buildSuggestion(achievementId);
            achievementMetric.setComplexityScore(suggestion.complexityScore());
            achievementMetric.setImpactScore(suggestion.impactScore());
        }
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
        AchievementMetric metric = achievementMetricRepository.findByAchievementId(achievementId).orElse(null);
        if (metric == null) {
            return null;
        }

        AchievementMetricSuggestionResponse suggestion = achievementMetricScoringService.buildSuggestion(achievementId);
        metric.setComplexityScore(suggestion.complexityScore());
        metric.setImpactScore(suggestion.impactScore());
        return achievementMetricRepository.save(metric);
    }

    @Override
    public AchievementMetricSuggestionResponse getSuggestedMetricByAchievementId(Long achievementId) {
        return achievementMetricScoringService.buildSuggestion(achievementId);
    }
}
