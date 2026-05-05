package tn.esprit.GestionPortfolio.DTO;

public record AchievementMetricSuggestionResponse(
        Integer complexityScore,
        Integer impactScore,
        Integer linkedSkillsCount,
        Integer highContributionCount,
        Integer mediumContributionCount,
        Integer lowContributionCount
) {
}
