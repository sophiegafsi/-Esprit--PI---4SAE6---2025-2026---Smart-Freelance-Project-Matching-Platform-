package tn.esprit.GestionPortfolio.Services;

import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.DTO.SkillCredibilityDto;
import tn.esprit.GestionPortfolio.DTO.SkillDTO;
import tn.esprit.GestionPortfolio.DTO.SkillRankingDto;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioSkillAnalyticsService {

    // Construit le Skill Credibility Score pour chaque skill utilisée
    // dans des achievements réels du freelancer.
    public List<SkillCredibilityDto> buildCredibilityRows(AnalyticsSnapshot snapshot) {
        Map<Long, List<AchievementSkill>> usagesBySkill = groupUsagesBySkill(snapshot.achievementSkills());
        List<SkillCredibilityDto> rows = new ArrayList<>();

        for (Map.Entry<Long, List<AchievementSkill>> entry : usagesBySkill.entrySet()) {
            SkillUsageSummary summary = summarizeSkillUsage(entry.getValue(), snapshot.metricsByAchievementId());
            rows.add(toCredibilityDto(entry.getKey(), summary, snapshot.skillsById()));
        }

        rows.sort(Comparator
                .comparing(SkillCredibilityDto::credibilityScore, Comparator.reverseOrder())
                .thenComparing(SkillCredibilityDto::occurrences, Comparator.reverseOrder())
                .thenComparing(SkillCredibilityDto::skillName, String.CASE_INSENSITIVE_ORDER));

        return rows;
    }

    // Produit le classement intelligent des skills à partir de la crédibilité,
    // de la fréquence d'utilisation et de la qualité des projets.
    public List<SkillRankingDto> buildRankingRows(List<SkillCredibilityDto> credibilityRows) {
        int maxOccurrences = findMaxOccurrences(credibilityRows);
        List<SkillRankingDto> rows = new ArrayList<>();

        for (SkillCredibilityDto row : credibilityRows) {
            double frequencyScore = (row.occurrences() * 100.0) / maxOccurrences;
            double contributionScore = (row.averageContributionWeight() / 3.0) * 100.0;
            double qualityScore = (row.projectQualityScore() / 10.0) * 100.0;
            double rankingScore = (row.credibilityScore() * 0.45)
                    + (frequencyScore * 0.30)
                    + (contributionScore * 0.15)
                    + (qualityScore * 0.10);

            rows.add(new SkillRankingDto(
                    0,
                    row.skillId(),
                    row.skillName(),
                    row.occurrences(),
                    row.credibilityScore(),
                    PortfolioAnalyticsMath.round(frequencyScore),
                    PortfolioAnalyticsMath.round(rankingScore)
            ));
        }

        rows.sort(Comparator
                .comparing(SkillRankingDto::rankingScore, Comparator.reverseOrder())
                .thenComparing(SkillRankingDto::credibilityScore, Comparator.reverseOrder())
                .thenComparing(SkillRankingDto::occurrences, Comparator.reverseOrder())
                .thenComparing(SkillRankingDto::skillName, String.CASE_INSENSITIVE_ORDER));

        return assignRanks(rows);
    }

    private Map<Long, List<AchievementSkill>> groupUsagesBySkill(List<AchievementSkill> achievementSkills) {
        Map<Long, List<AchievementSkill>> grouped = new HashMap<>();
        for (AchievementSkill achievementSkill : achievementSkills) {
            if (achievementSkill.getSkillId() == null) {
                continue;
            }
            grouped.computeIfAbsent(achievementSkill.getSkillId(), ignored -> new ArrayList<>())
                    .add(achievementSkill);
        }
        return grouped;
    }

    // Résume toutes les utilisations d'une skill avant de calculer son score final.
    private SkillUsageSummary summarizeSkillUsage(List<AchievementSkill> usages, Map<Long, AchievementMetric> metricsByAchievementId) {
        int occurrences = usages.size();
        if (occurrences == 0) {
            return SkillUsageSummary.empty();
        }

        double totalContribution = 0.0;
        double totalComplexity = 0.0;
        double totalImpact = 0.0;
        double totalQuality = 0.0;
        double totalUsageSignal = 0.0;

        for (AchievementSkill usage : usages) {
            AchievementMetric metric = metricFor(metricsByAchievementId, usage.getAchievement());
            double contribution = PortfolioAnalyticsMath.contributionWeight(usage.getContributionLevel());
            double complexity = PortfolioAnalyticsMath.metricComplexity(metric);
            double impact = PortfolioAnalyticsMath.metricImpact(metric);
            double quality = PortfolioAnalyticsMath.projectQuality(metric);

            totalContribution += contribution;
            totalComplexity += complexity;
            totalImpact += impact;
            totalQuality += quality;
            totalUsageSignal += contribution * quality;
        }

        return new SkillUsageSummary(
                occurrences,
                PortfolioAnalyticsMath.average(totalContribution, occurrences),
                PortfolioAnalyticsMath.average(totalComplexity, occurrences),
                PortfolioAnalyticsMath.average(totalImpact, occurrences),
                PortfolioAnalyticsMath.average(totalQuality, occurrences),
                PortfolioAnalyticsMath.average(totalUsageSignal, occurrences)
        );
    }

    // Formule simple : signal d'usage réel + bonus de répétition.
    private SkillCredibilityDto toCredibilityDto(Long skillId, SkillUsageSummary summary, Map<Long, SkillDTO> skillsById) {
        double repetitionBonus = PortfolioAnalyticsMath.componentScore(
                summary.occurrences(),
                PortfolioAnalyticsMath.MAX_OCCURRENCES_FOR_BONUS,
                20.0
        );

        double credibilityScore = Math.min(
                PortfolioAnalyticsMath.MAX_SCORE,
                (summary.usageSignal() / 30.0) * 80.0 + repetitionBonus
        );

        return new SkillCredibilityDto(
                skillId,
                resolveSkillName(skillId, skillsById),
                summary.occurrences(),
                PortfolioAnalyticsMath.round(summary.averageContributionWeight()),
                PortfolioAnalyticsMath.round(summary.averageComplexityScore()),
                PortfolioAnalyticsMath.round(summary.averageImpactScore()),
                PortfolioAnalyticsMath.round(summary.projectQualityScore()),
                PortfolioAnalyticsMath.round(credibilityScore)
        );
    }

    private String resolveSkillName(Long skillId, Map<Long, SkillDTO> skillsById) {
        SkillDTO skill = skillsById.get(skillId);
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) {
            return "Skill #" + skillId;
        }
        return skill.getName();
    }

    private AchievementMetric metricFor(Map<Long, AchievementMetric> metricsByAchievementId, Achievement achievement) {
        if (achievement == null || achievement.getId() == null) {
            return null;
        }
        return metricsByAchievementId.get(achievement.getId());
    }

    private int findMaxOccurrences(List<SkillCredibilityDto> credibilityRows) {
        int max = 1;
        for (SkillCredibilityDto row : credibilityRows) {
            max = Math.max(max, row.occurrences());
        }
        return max;
    }

    private List<SkillRankingDto> assignRanks(List<SkillRankingDto> rows) {
        List<SkillRankingDto> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            SkillRankingDto row = rows.get(i);
            ranked.add(new SkillRankingDto(
                    i + 1,
                    row.skillId(),
                    row.skillName(),
                    row.occurrences(),
                    row.credibilityScore(),
                    row.frequencyScore(),
                    row.rankingScore()
            ));
        }
        return ranked;
    }

    private record SkillUsageSummary(
            int occurrences,
            double averageContributionWeight,
            double averageComplexityScore,
            double averageImpactScore,
            double projectQualityScore,
            double usageSignal
    ) {
        private static SkillUsageSummary empty() {
            return new SkillUsageSummary(0, 0, 0, 0, 0, 0);
        }
    }
}
