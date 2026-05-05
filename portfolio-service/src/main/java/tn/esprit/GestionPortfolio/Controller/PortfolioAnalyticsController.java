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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @RequestParam(required = false) String freelancerId,
            @RequestParam(required = false) Integer limit
    ) {
        return portfolioAnalyticsService.getSkillCredibility(freelancerId, limit);
    }

    @GetMapping("/skills/ranking")
    public List<SkillRankingDto> getSkillRanking(
            @RequestParam(required = false) String freelancerId,
            @RequestParam(required = false) Integer limit
    ) {
        return portfolioAnalyticsService.getSkillRanking(freelancerId, limit);
    }

    @GetMapping("/profile/score")
    public ProfileStrengthDto getProfileStrength(@RequestParam(required = false) String freelancerId) {
        return portfolioAnalyticsService.getProfileStrength(freelancerId);
    }

    @GetMapping("/profile/statistics")
    public ProfileStatisticsDto getProfileStatistics(@RequestParam(required = false) String freelancerId) {
        return portfolioAnalyticsService.getProfileStatistics(freelancerId);
    }

    @GetMapping(value = "/profile/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPortfolioAnalyticsPdf(@RequestParam(required = false) String freelancerId) {
        String id = freelancerId;
        if (id == null || id.trim().isEmpty() || id.equals("0")) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Jwt jwt) {
                id = jwt.getSubject();
            }
        }
        
        byte[] pdf = portfolioAnalyticsPdfService.exportReport(id);
        String filename = "portfolio-analytics-" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
