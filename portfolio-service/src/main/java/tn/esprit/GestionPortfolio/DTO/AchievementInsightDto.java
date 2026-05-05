package tn.esprit.GestionPortfolio.DTO;

public record AchievementInsightDto(
        Long achievementId,
        String title,
        Integer linkedSkillsCount,
        Double qualityScore
) {
}
