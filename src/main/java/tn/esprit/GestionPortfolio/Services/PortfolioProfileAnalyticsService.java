package tn.esprit.GestionPortfolio.Services;

import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.DTO.AchievementInsightDto;
import tn.esprit.GestionPortfolio.DTO.AchievementTimelineDto;
import tn.esprit.GestionPortfolio.DTO.ContributionDistributionDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStatisticsDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStrengthDto;
import tn.esprit.GestionPortfolio.DTO.SkillCredibilityDto;
import tn.esprit.GestionPortfolio.DTO.SkillRankingDto;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Entities.ContributionLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PortfolioProfileAnalyticsService {

    // Calcule un score global du profil sur 100 à partir du volume,
    // de la diversité, de la contribution et de la qualité des projets.
    public ProfileStrengthDto buildProfileStrength(Long freelancerId, AnalyticsSnapshot snapshot) {
        ProfileStrengthBreakdown breakdown = buildBreakdown(snapshot);

        return new ProfileStrengthDto(
                freelancerId,
                breakdown.achievementsCount(),
                breakdown.distinctSkillsCount(),
                breakdown.averageContributionWeight(),
                breakdown.averageProjectQuality(),
                breakdown.achievementsComponent(),
                breakdown.diversityComponent(),
                breakdown.contributionComponent(),
                breakdown.qualityComponent(),
                breakdown.overallScore(),
                breakdown.profileLevel()
        );
    }

    // Retourne les statistiques complémentaires affichées sur le dashboard.
    public ProfileStatisticsDto buildProfileStatistics(
            Long freelancerId,
            AnalyticsSnapshot snapshot,
            List<SkillCredibilityDto> credibilityRows,
            List<SkillRankingDto> rankingRows
    ) {
        return new ProfileStatisticsDto(
                freelancerId,
                snapshot.achievements().size(),
                distinctSkillsCount(snapshot.achievementSkills()),
                PortfolioAnalyticsMath.round(averageMetricComplexity(snapshot.metricsByAchievementId().values().stream().toList())),
                PortfolioAnalyticsMath.round(averageMetricImpact(snapshot.metricsByAchievementId().values().stream().toList())),
                PortfolioAnalyticsMath.round(averageMetricDuration(snapshot.metricsByAchievementId().values().stream().toList())),
                rankingRows.isEmpty() ? null : rankingRows.get(0),
                credibilityRows.isEmpty() ? null : credibilityRows.get(0),
                strongestAchievement(snapshot),
                buildContributionDistribution(snapshot.achievementSkills()),
                buildTimeline(snapshot)
        );
    }

    private ProfileStrengthBreakdown buildBreakdown(AnalyticsSnapshot snapshot) {
        int achievementsCount = snapshot.achievements().size();
        int distinctSkillsCount = distinctSkillsCount(snapshot.achievementSkills());
        double averageContributionWeight = averageContributionWeight(snapshot.achievementSkills());
        double averageProjectQuality = averageProjectQuality(snapshot);

        double achievementsComponent = PortfolioAnalyticsMath.componentScore(
                achievementsCount,
                PortfolioAnalyticsMath.MAX_ACHIEVEMENTS_FOR_FULL_SCORE,
                PortfolioAnalyticsMath.FULL_COMPONENT_SCORE
        );
        double diversityComponent = PortfolioAnalyticsMath.componentScore(
                distinctSkillsCount,
                PortfolioAnalyticsMath.MAX_SKILLS_FOR_FULL_SCORE,
                PortfolioAnalyticsMath.FULL_COMPONENT_SCORE
        );
        double contributionComponent = (averageContributionWeight / 3.0) * PortfolioAnalyticsMath.FULL_COMPONENT_SCORE;
        double qualityComponent = (averageProjectQuality / 10.0) * PortfolioAnalyticsMath.FULL_COMPONENT_SCORE;
        double overallScore = PortfolioAnalyticsMath.round(
                achievementsComponent + diversityComponent + contributionComponent + qualityComponent
        );

        return new ProfileStrengthBreakdown(
                achievementsCount,
                distinctSkillsCount,
                PortfolioAnalyticsMath.round(averageContributionWeight),
                PortfolioAnalyticsMath.round(averageProjectQuality),
                PortfolioAnalyticsMath.round(achievementsComponent),
                PortfolioAnalyticsMath.round(diversityComponent),
                PortfolioAnalyticsMath.round(contributionComponent),
                PortfolioAnalyticsMath.round(qualityComponent),
                overallScore,
                PortfolioAnalyticsMath.profileLevel(overallScore)
        );
    }

    private int distinctSkillsCount(List<AchievementSkill> achievementSkills) {
        Set<Long> skillIds = new HashSet<>();
        for (AchievementSkill achievementSkill : achievementSkills) {
            if (achievementSkill.getSkillId() != null) {
                skillIds.add(achievementSkill.getSkillId());
            }
        }
        return skillIds.size();
    }

    private double averageContributionWeight(List<AchievementSkill> achievementSkills) {
        if (achievementSkills.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (AchievementSkill achievementSkill : achievementSkills) {
            total += PortfolioAnalyticsMath.contributionWeight(achievementSkill.getContributionLevel());
        }
        return PortfolioAnalyticsMath.average(total, achievementSkills.size());
    }

    private double averageProjectQuality(AnalyticsSnapshot snapshot) {
        if (snapshot.achievements().isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Achievement achievement : snapshot.achievements()) {
            total += PortfolioAnalyticsMath.projectQuality(metricFor(snapshot.metricsByAchievementId(), achievement));
        }
        return PortfolioAnalyticsMath.average(total, snapshot.achievements().size());
    }

    private double averageMetricComplexity(List<AchievementMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (AchievementMetric metric : metrics) {
            total += PortfolioAnalyticsMath.metricComplexity(metric);
        }
        return PortfolioAnalyticsMath.average(total, metrics.size());
    }

    private double averageMetricImpact(List<AchievementMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (AchievementMetric metric : metrics) {
            total += PortfolioAnalyticsMath.metricImpact(metric);
        }
        return PortfolioAnalyticsMath.average(total, metrics.size());
    }

    private double averageMetricDuration(List<AchievementMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (AchievementMetric metric : metrics) {
            total += PortfolioAnalyticsMath.metricDuration(metric);
        }
        return PortfolioAnalyticsMath.average(total, metrics.size());
    }

    private List<ContributionDistributionDto> buildContributionDistribution(List<AchievementSkill> achievementSkills) {
        Map<ContributionLevel, Long> counts = new EnumMap<>(ContributionLevel.class);
        for (ContributionLevel level : ContributionLevel.values()) {
            counts.put(level, 0L);
        }

        for (AchievementSkill achievementSkill : achievementSkills) {
            if (achievementSkill.getContributionLevel() == null) {
                continue;
            }
            ContributionLevel level = achievementSkill.getContributionLevel();
            counts.put(level, counts.get(level) + 1);
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        List<ContributionDistributionDto> rows = new ArrayList<>();

        for (ContributionLevel level : ContributionLevel.values()) {
            long count = counts.get(level);
            double percentage = total == 0 ? 0.0 : PortfolioAnalyticsMath.round((count * 100.0) / total);
            rows.add(new ContributionDistributionDto(level.name(), count, percentage));
        }

        return rows;
    }

    private List<AchievementTimelineDto> buildTimeline(AnalyticsSnapshot snapshot) {
        Map<String, TimelineBucket> buckets = new HashMap<>();

        for (Achievement achievement : snapshot.achievements()) {
            String period = PortfolioAnalyticsMath.formatPeriod(achievement.getCompletionDate());
            TimelineBucket bucket = buckets.computeIfAbsent(period, ignored -> new TimelineBucket());
            bucket.count++;
            bucket.totalQuality += PortfolioAnalyticsMath.projectQuality(metricFor(snapshot.metricsByAchievementId(), achievement));
        }

        List<String> periods = new ArrayList<>(buckets.keySet());
        periods.sort(String::compareTo);

        List<AchievementTimelineDto> rows = new ArrayList<>();
        for (String period : periods) {
            TimelineBucket bucket = buckets.get(period);
            rows.add(new AchievementTimelineDto(
                    period,
                    bucket.count,
                    PortfolioAnalyticsMath.round(PortfolioAnalyticsMath.average(bucket.totalQuality, bucket.count))
            ));
        }

        return rows;
    }

    // Cherche l'achievement le plus solide selon la qualité du projet,
    // puis le nombre de skills effectivement mobilisées.
    private AchievementInsightDto strongestAchievement(AnalyticsSnapshot snapshot) {
        if (snapshot.achievements().isEmpty()) {
            return null;
        }

        Map<Long, Integer> linkedSkillsCount = linkedSkillsCountByAchievement(snapshot.achievementSkills());
        AchievementInsightDto best = null;

        for (Achievement achievement : snapshot.achievements()) {
            AchievementInsightDto candidate = new AchievementInsightDto(
                    achievement.getId(),
                    achievement.getTitle(),
                    linkedSkillsCount.getOrDefault(achievement.getId(), 0),
                    PortfolioAnalyticsMath.round(
                            PortfolioAnalyticsMath.projectQuality(metricFor(snapshot.metricsByAchievementId(), achievement))
                    )
            );

            if (best == null || isStronger(candidate, best)) {
                best = candidate;
            }
        }

        return best;
    }

    private Map<Long, Integer> linkedSkillsCountByAchievement(List<AchievementSkill> achievementSkills) {
        Map<Long, Integer> result = new HashMap<>();
        for (AchievementSkill achievementSkill : achievementSkills) {
            if (achievementSkill.getAchievement() == null || achievementSkill.getAchievement().getId() == null) {
                continue;
            }
            Long achievementId = achievementSkill.getAchievement().getId();
            result.put(achievementId, result.getOrDefault(achievementId, 0) + 1);
        }
        return result;
    }

    private boolean isStronger(AchievementInsightDto candidate, AchievementInsightDto current) {
        return Comparator
                .comparing(AchievementInsightDto::qualityScore)
                .thenComparing(AchievementInsightDto::linkedSkillsCount)
                .compare(candidate, current) > 0;
    }

    private AchievementMetric metricFor(Map<Long, AchievementMetric> metricsByAchievementId, Achievement achievement) {
        if (achievement == null || achievement.getId() == null) {
            return null;
        }
        return metricsByAchievementId.get(achievement.getId());
    }

    private record ProfileStrengthBreakdown(
            int achievementsCount,
            int distinctSkillsCount,
            double averageContributionWeight,
            double averageProjectQuality,
            double achievementsComponent,
            double diversityComponent,
            double contributionComponent,
            double qualityComponent,
            double overallScore,
            String profileLevel
    ) {
    }

    private static final class TimelineBucket {
        private int count;
        private double totalQuality;
    }
}
