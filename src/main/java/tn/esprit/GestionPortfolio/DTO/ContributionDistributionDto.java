package tn.esprit.GestionPortfolio.DTO;

public record ContributionDistributionDto(
        String level,
        Long count,
        Double percentage
) {
}
