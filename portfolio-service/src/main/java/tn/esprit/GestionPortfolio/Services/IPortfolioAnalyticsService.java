package tn.esprit.GestionPortfolio.Services;

import tn.esprit.GestionPortfolio.DTO.ProfileStatisticsDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStrengthDto;
import tn.esprit.GestionPortfolio.DTO.SkillCredibilityDto;
import tn.esprit.GestionPortfolio.DTO.SkillRankingDto;

import java.util.List;

public interface IPortfolioAnalyticsService {
    List<SkillCredibilityDto> getSkillCredibility(String freelancerId, Integer limit);
    List<SkillRankingDto> getSkillRanking(String freelancerId, Integer limit);
    ProfileStrengthDto getProfileStrength(String freelancerId);
    ProfileStatisticsDto getProfileStatistics(String freelancerId);
}
