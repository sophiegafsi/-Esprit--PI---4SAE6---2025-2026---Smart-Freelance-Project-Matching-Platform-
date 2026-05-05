package tn.esprit.GestionPortfolio.DTO;

public record SkillRankingDto(
        Integer rank,
        Long skillId,
        String skillName,
        Integer occurrences,
        Double credibilityScore,
        Double frequencyScore,
        Double rankingScore
) {
}
