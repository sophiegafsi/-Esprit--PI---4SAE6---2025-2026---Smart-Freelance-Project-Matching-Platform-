package tn.esprit.GestionPortfolio.DTO;

public record TextAssistantResponse(
        String originalText,
        String transformedText,
        String operation,
        String targetLanguage,
        boolean changed
) {
}
