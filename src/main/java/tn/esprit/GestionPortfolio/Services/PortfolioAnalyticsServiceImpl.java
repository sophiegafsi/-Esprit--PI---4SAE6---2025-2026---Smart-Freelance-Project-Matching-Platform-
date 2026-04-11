package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.GestionPortfolio.DTO.ProfileStatisticsDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStrengthDto;
import tn.esprit.GestionPortfolio.DTO.SkillCredibilityDto;
import tn.esprit.GestionPortfolio.DTO.SkillRankingDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioAnalyticsServiceImpl implements IPortfolioAnalyticsService {

    // Cette classe reste volontairement légère :
    // elle orchestre les sous-services sans contenir les calculs métier.
    private final PortfolioAnalyticsLoaderService loaderService;
    private final PortfolioSkillAnalyticsService skillAnalyticsService;
    private final PortfolioProfileAnalyticsService profileAnalyticsService;

    @Override
    public List<SkillCredibilityDto> getSkillCredibility(Long freelancerId, Integer limit) {
        AnalyticsSnapshot snapshot = loaderService.loadSnapshot(freelancerId);
        return limitRows(skillAnalyticsService.buildCredibilityRows(snapshot), limit);
    }

    @Override
    public List<SkillRankingDto> getSkillRanking(Long freelancerId, Integer limit) {
        AnalyticsSnapshot snapshot = loaderService.loadSnapshot(freelancerId);
        List<SkillCredibilityDto> credibilityRows = skillAnalyticsService.buildCredibilityRows(snapshot);
        return limitRows(skillAnalyticsService.buildRankingRows(credibilityRows), limit);
    }

    @Override
    public ProfileStrengthDto getProfileStrength(Long freelancerId) {
        AnalyticsSnapshot snapshot = loaderService.loadSnapshot(freelancerId);
        return profileAnalyticsService.buildProfileStrength(freelancerId, snapshot);
    }

    @Override
    public ProfileStatisticsDto getProfileStatistics(Long freelancerId) {
        AnalyticsSnapshot snapshot = loaderService.loadSnapshot(freelancerId);
        List<SkillCredibilityDto> credibilityRows = skillAnalyticsService.buildCredibilityRows(snapshot);
        List<SkillRankingDto> rankingRows = skillAnalyticsService.buildRankingRows(credibilityRows);
        return profileAnalyticsService.buildProfileStatistics(freelancerId, snapshot, credibilityRows, rankingRows);
    }

    private <T> List<T> limitRows(List<T> rows, Integer limit) {
        if (limit == null || limit <= 0 || rows.size() <= limit) {
            return rows;
        }
        return rows.subList(0, limit);
    }
}
