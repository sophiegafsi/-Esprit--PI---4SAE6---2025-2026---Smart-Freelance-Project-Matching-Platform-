package tn.esprit.GestionPortfolio.DTO;

public record GenerateAchievementDescriptionResponse(
        Long achievementId,
        String title,
        String generatedDescription
) {
}
