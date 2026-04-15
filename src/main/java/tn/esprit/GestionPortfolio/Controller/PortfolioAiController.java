package tn.esprit.GestionPortfolio.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.GestionPortfolio.DTO.GenerateAchievementDescriptionRequest;
import tn.esprit.GestionPortfolio.DTO.GenerateAchievementDescriptionResponse;
import tn.esprit.GestionPortfolio.DTO.SpringAiReviewRequest;
import tn.esprit.GestionPortfolio.DTO.SpringAiReviewResponse;
import tn.esprit.GestionPortfolio.DTO.TextAssistantRequest;
import tn.esprit.GestionPortfolio.DTO.TextAssistantResponse;
import tn.esprit.GestionPortfolio.Services.AiAchievementService;
import tn.esprit.GestionPortfolio.Services.AiTextAssistantService;
import tn.esprit.GestionPortfolio.Services.SpringAiPortfolioAssistantService;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PortfolioAiController {

    private final AiAchievementService aiAchievementService;
    private final AiTextAssistantService aiTextAssistantService;
    private final SpringAiPortfolioAssistantService springAiPortfolioAssistantService;

    @PostMapping("/generate-description")
    public GenerateAchievementDescriptionResponse generateAchievementDescription(
            @RequestBody GenerateAchievementDescriptionRequest request
    ) {
        return aiAchievementService.generateDescription(request);
    }

    @PostMapping("/rewrite-text")
    public TextAssistantResponse rewriteText(@RequestBody TextAssistantRequest request) {
        return aiTextAssistantService.rewriteText(request);
    }

    @PostMapping("/translate-text")
    public TextAssistantResponse translateText(@RequestBody TextAssistantRequest request) {
        return aiTextAssistantService.translateText(request);
    }

    @PostMapping("/mask-bad-words")
    public TextAssistantResponse maskBadWords(@RequestBody TextAssistantRequest request) {
        return aiTextAssistantService.maskBadWords(request);
    }

    @PostMapping("/spring-review")
    public SpringAiReviewResponse reviewWithSpringAi(@RequestBody SpringAiReviewRequest request) {
        return springAiPortfolioAssistantService.reviewAchievement(request);
    }
}
