package tn.esprit.GestionPortfolio.DTO;

public record SpringAiReviewResponse(
        String title,
        String description,
        String feedback,
        String provider,
        String model,
        boolean fallbackUsed,
        boolean available
) {
}
