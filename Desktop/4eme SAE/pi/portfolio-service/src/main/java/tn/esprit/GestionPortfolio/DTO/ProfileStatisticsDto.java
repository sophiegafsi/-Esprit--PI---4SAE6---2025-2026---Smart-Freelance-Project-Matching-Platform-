package tn.esprit.GestionPortfolio.DTO;

import java.util.List;

public record ProfileStatisticsDto(
        String freelancerId,
        Integer achievementsCount,
        Integer distinctSkillsCount,
        Double averageComplexityScore,
        Double averageImpactScore,
        Double averageDurationDays,
        SkillRankingDto topRankedSkill,
        SkillCredibilityDto mostCredibleSkill,
        AchievementInsightDto strongestAchievement,
        List<ContributionDistributionDto> contributionDistribution,
        List<AchievementTimelineDto> timeline
) {
}
