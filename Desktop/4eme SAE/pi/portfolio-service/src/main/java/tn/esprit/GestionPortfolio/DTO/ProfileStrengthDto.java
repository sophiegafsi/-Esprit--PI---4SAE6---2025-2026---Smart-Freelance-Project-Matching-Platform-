package tn.esprit.GestionPortfolio.DTO;

public record ProfileStrengthDto(
        String freelancerId,
        Integer achievementsCount,
        Integer distinctSkillsCount,
        Double averageContributionWeight,
        Double averageProjectQuality,
        Double achievementsComponent,
        Double diversityComponent,
        Double contributionComponent,
        Double qualityComponent,
        Double overallScore,
        String profileLevel
) {
}
