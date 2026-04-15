package tn.esprit.GestionPortfolio.DTO;

public record SkillCredibilityDto(
        Long skillId,
        String skillName,
        Integer occurrences,
        Double averageContributionWeight,
        Double averageComplexityScore,
        Double averageImpactScore,
        Double projectQualityScore,
        Double credibilityScore
) {
}
