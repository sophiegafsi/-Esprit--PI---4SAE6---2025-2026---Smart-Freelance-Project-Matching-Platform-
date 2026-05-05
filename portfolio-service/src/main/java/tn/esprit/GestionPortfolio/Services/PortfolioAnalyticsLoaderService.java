package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.Client.SkillClient;
import tn.esprit.GestionPortfolio.DTO.SkillDTO;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Repository.AchievementMetricRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementSkillRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PortfolioAnalyticsLoaderService {

    private final AchievementRepository achievementRepository;
    private final AchievementSkillRepository achievementSkillRepository;
    private final AchievementMetricRepository achievementMetricRepository;
    private final SkillClient skillClient;

    // Charge en une seule étape les achievements, skills liées, métriques
    // et catalogue des skills venant du microservice skills-service.
    public AnalyticsSnapshot loadSnapshot(String freelancerId) {
        List<Achievement> achievements = achievementRepository.findByFreelancerId(freelancerId);
        if (achievements.isEmpty()) {
            return AnalyticsSnapshot.empty();
        }

        List<Long> achievementIds = achievements.stream()
                .map(Achievement::getId)
                .filter(Objects::nonNull)
                .toList();

        List<AchievementSkill> achievementSkills = achievementIds.isEmpty()
                ? List.of()
                : achievementSkillRepository.findByAchievementIdIn(achievementIds);

        List<AchievementMetric> metrics = achievementIds.isEmpty()
                ? List.of()
                : achievementMetricRepository.findByAchievementIdIn(achievementIds);

        return new AnalyticsSnapshot(
                achievements,
                achievementSkills,
                mapMetricsByAchievement(metrics),
                fetchSkillCatalog(achievementSkills)
        );
    }

    private Map<Long, AchievementMetric> mapMetricsByAchievement(List<AchievementMetric> metrics) {
        Map<Long, AchievementMetric> result = new HashMap<>();
        for (AchievementMetric metric : metrics) {
            if (metric.getAchievement() == null || metric.getAchievement().getId() == null) {
                continue;
            }
            result.putIfAbsent(metric.getAchievement().getId(), metric);
        }
        return result;
    }

    private Map<Long, SkillDTO> fetchSkillCatalog(Collection<AchievementSkill> achievementSkills) {
        try {
            return loadSkillsFromSkillService();
        } catch (Exception ex) {
            // On garde une solution de secours pour que l'analyse reste disponible
            // même si le microservice skills-service ne répond pas.
            return buildFallbackSkillCatalog(achievementSkills);
        }
    }

    private Map<Long, SkillDTO> loadSkillsFromSkillService() {
        Map<Long, SkillDTO> result = new HashMap<>();
        for (SkillDTO skill : skillClient.getAllSkills()) {
            if (skill.getId() != null) {
                result.putIfAbsent(skill.getId(), skill);
            }
        }
        return result;
    }

    private Map<Long, SkillDTO> buildFallbackSkillCatalog(Collection<AchievementSkill> achievementSkills) {
        Map<Long, SkillDTO> result = new HashMap<>();
        for (AchievementSkill achievementSkill : achievementSkills) {
            Long skillId = achievementSkill.getSkillId();
            if (skillId == null) {
                continue;
            }
            result.putIfAbsent(skillId, new SkillDTO(skillId, "Skill #" + skillId, null, null, null));
        }
        return result;
    }
}
