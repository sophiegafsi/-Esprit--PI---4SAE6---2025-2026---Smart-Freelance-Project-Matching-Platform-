package tn.esprit.GestionPortfolio.DTO;

public record AchievementTimelineDto(
        String period,
        Integer achievementsCount,
        Double averageQualityScore
) {
}
