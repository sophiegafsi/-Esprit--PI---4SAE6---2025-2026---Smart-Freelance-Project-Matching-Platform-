package tn.esprit.GestionPortfolio.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.GestionPortfolio.DTO.ProfileStatisticsDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStrengthDto;
import tn.esprit.GestionPortfolio.DTO.SkillCredibilityDto;
import tn.esprit.GestionPortfolio.DTO.SkillRankingDto;
import tn.esprit.GestionPortfolio.Services.IPortfolioAnalyticsService;
import tn.esprit.GestionPortfolio.Services.PortfolioAnalyticsPdfService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin("*")
public class PortfolioAnalyticsController {

    private final IPortfolioAnalyticsService portfolioAnalyticsService;
    private final PortfolioAnalyticsPdfService portfolioAnalyticsPdfService;

    @GetMapping("/skills/credibility")
    public List<SkillCredibilityDto> getSkillCredibility(
            @RequestParam Long freelancerId,
            @RequestParam(required = false) Integer limit
    ) {
        return portfolioAnalyticsService.getSkillCredibility(freelancerId, limit);
    }

    @GetMapping("/skills/ranking")
    public List<SkillRankingDto> getSkillRanking(
            @RequestParam Long freelancerId,
            @RequestParam(required = false) Integer limit
    ) {
        return portfolioAnalyticsService.getSkillRanking(freelancerId, limit);
    }

    @GetMapping("/profile/score")
    public ProfileStrengthDto getProfileStrength(@RequestParam Long freelancerId) {
        return portfolioAnalyticsService.getProfileStrength(freelancerId);
    }

    @GetMapping("/profile/statistics")
    public ProfileStatisticsDto getProfileStatistics(@RequestParam Long freelancerId) {
        return portfolioAnalyticsService.getProfileStatistics(freelancerId);
    }

    @GetMapping(value = "/profile/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPortfolioAnalyticsPdf(@RequestParam Long freelancerId) {
        byte[] pdf = portfolioAnalyticsPdfService.exportReport(freelancerId);
        String filename = "portfolio-analytics-" + freelancerId + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
