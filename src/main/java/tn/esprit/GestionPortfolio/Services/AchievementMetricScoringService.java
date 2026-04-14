package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.GestionPortfolio.DTO.AchievementMetricSuggestionResponse;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Entities.ContributionLevel;
import tn.esprit.GestionPortfolio.Repository.AchievementMetricRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementSkillRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementMetricScoringService {

    private final AchievementSkillRepository achievementSkillRepository;
    private final AchievementMetricRepository achievementMetricRepository;

    public AchievementMetricSuggestionResponse buildSuggestion(Long achievementId) {
        List<AchievementSkill> linkedSkills = achievementSkillRepository.findByAchievementId(achievementId);

        int skillCount = linkedSkills.size();
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (AchievementSkill row : linkedSkills) {
            ContributionLevel level = row.getContributionLevel();
            if (level == ContributionLevel.HIGH) {
                highCount++;
            } else if (level == ContributionLevel.MEDIUM) {
                mediumCount++;
            } else if (level == ContributionLevel.LOW) {
                lowCount++;
            }
        }

        int complexityScore = computeComplexityScore(skillCount, highCount, mediumCount);
        int impactScore = computeImpactScore(skillCount, highCount, mediumCount, lowCount);

        return new AchievementMetricSuggestionResponse(
                complexityScore,
                impactScore,
                skillCount,
                highCount,
                mediumCount,
                lowCount
        );
    }

    @Transactional
    public void syncMetricForAchievement(Long achievementId) {
        AchievementMetric metric = achievementMetricRepository.findByAchievementId(achievementId).orElse(null);
        if (metric == null) {
            return;
        }

        AchievementMetricSuggestionResponse suggestion = buildSuggestion(achievementId);
        metric.setComplexityScore(suggestion.complexityScore());
        metric.setImpactScore(suggestion.impactScore());
        achievementMetricRepository.save(metric);
    }

    private int computeComplexityScore(int skillCount, int highCount, int mediumCount) {
        if (skillCount <= 0) {
            return 1;
        }

        double rawScore = 1.0 + (skillCount * 1.35) + (highCount * 0.9) + (mediumCount * 0.45);
        return clampScore(rawScore);
    }

    private int computeImpactScore(int skillCount, int highCount, int mediumCount, int lowCount) {
        if (skillCount <= 0) {
            return 1;
        }

        double rawScore = 1.0 + (skillCount * 1.15) + (highCount * 1.25) + (mediumCount * 0.55) + (lowCount * 0.2);
        return clampScore(rawScore);
    }

    private int clampScore(double rawScore) {
        return Math.max(1, Math.min(10, (int) Math.round(rawScore)));
    }
}
