package tn.esprit.GestionPortfolio.Services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.GestionPortfolio.Client.SkillClient;
import tn.esprit.GestionPortfolio.DTO.GenerateAchievementDescriptionRequest;
import tn.esprit.GestionPortfolio.DTO.GenerateAchievementDescriptionResponse;
import tn.esprit.GestionPortfolio.DTO.SkillDTO;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Entities.ContributionLevel;
import tn.esprit.GestionPortfolio.Repository.AchievementMetricRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementSkillRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiAchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementSkillRepository achievementSkillRepository;
    private final AchievementMetricRepository achievementMetricRepository;
    private final SkillClient skillClient;

    public AiAchievementService(
            AchievementRepository achievementRepository,
            AchievementSkillRepository achievementSkillRepository,
            AchievementMetricRepository achievementMetricRepository,
            SkillClient skillClient
    ) {
        this.achievementRepository = achievementRepository;
        this.achievementSkillRepository = achievementSkillRepository;
        this.achievementMetricRepository = achievementMetricRepository;
        this.skillClient = skillClient;
    }

    // Generates a portfolio description locally from the achievement,
    // its linked skills, and the project metrics.
    public GenerateAchievementDescriptionResponse generateDescription(GenerateAchievementDescriptionRequest request) {
        Long achievementId = request == null ? null : request.achievementId();
        if (achievementId == null || achievementId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "achievementId must be a positive value.");
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Achievement not found."));

        List<AchievementSkill> achievementSkills = achievementSkillRepository.findByAchievementId(achievementId);
        AchievementMetric metric = achievementMetricRepository.findByAchievementId(achievementId).orElse(null);
        Map<Long, SkillDTO> skillsById = loadSkillCatalog(achievementSkills);

        String cleanedDescription = buildLocalDescription(achievement, achievementSkills, metric, skillsById);

        return new GenerateAchievementDescriptionResponse(
                achievement.getId(),
                achievement.getTitle(),
                cleanedDescription
        );
    }

    // Offline generator used without any external AI provider.
    private String buildLocalDescription(
            Achievement achievement,
            List<AchievementSkill> achievementSkills,
            AchievementMetric metric,
            Map<Long, SkillDTO> skillsById
    ) {
        List<AchievementSkill> sortedSkills = new ArrayList<>(achievementSkills);
        sortedSkills.sort(Comparator
                .comparing((AchievementSkill row) -> contributionScore(row.getContributionLevel())).reversed()
                .thenComparing(row -> resolveSkillName(row.getSkillId(), skillsById), String.CASE_INSENSITIVE_ORDER));

        String title = valueOrFallback(achievement.getTitle(), "Ce projet");
        String objective = objectivePhrase(achievement.getDescription());
        String skillsSummary = buildSkillsSummary(sortedSkills, skillsById);
        String contributionSummary = buildContributionSummary(sortedSkills);
        String metricsSummary = buildMetricsSummary(metric);

        String description = title + " is a portfolio project " + objective
                + ". It highlights " + skillsSummary
                + ". My contribution was " + contributionSummary
                + metricsSummary;

        return sanitizeModelOutput(description);
    }

    private String objectivePhrase(String rawDescription) {
        String description = valueOrFallback(rawDescription, "");
        if (description.isBlank()) {
            return "designed to showcase a concrete, result-oriented delivery";
        }
        String normalized = description.substring(0, 1).toLowerCase() + description.substring(1);
        return "focused on " + normalized;
    }

    private String buildSkillsSummary(List<AchievementSkill> achievementSkills, Map<Long, SkillDTO> skillsById) {
        if (achievementSkills.isEmpty()) {
            return "une approche polyvalente du projet";
        }

        List<String> names = achievementSkills.stream()
                .map(row -> resolveSkillName(row.getSkillId(), skillsById))
                .distinct()
                .limit(4)
                .toList();

        String joinedNames = joinEnglishList(names);
        if (achievementSkills.size() == 1) {
            return "the skill " + joinedNames;
        }
        return "the skills " + joinedNames;
    }

    private String buildContributionSummary(List<AchievementSkill> achievementSkills) {
        if (achievementSkills.isEmpty()) {
            return "structured mainly around requirement framing and delivery support";
        }

        long highCount = achievementSkills.stream().filter(row -> row.getContributionLevel() == ContributionLevel.HIGH).count();
        long mediumCount = achievementSkills.stream().filter(row -> row.getContributionLevel() == ContributionLevel.MEDIUM).count();

        if (highCount > 0) {
            List<String> highSkills = achievementSkills.stream()
                    .filter(row -> row.getContributionLevel() == ContributionLevel.HIGH)
                    .map(row -> row.getSkillId())
                    .distinct()
                    .map(id -> "of high-responsibility technical areas")
                    .limit(1)
                    .toList();
            return "strong, with direct ownership " + joinEnglishList(highSkills)
                    + (mediumCount > 0 ? " and additional support across complementary technical areas" : "");
        }
        if (mediumCount > 0) {
            return "balanced, with meaningful contribution across several parts of the project";
        }
        return "targeted, mainly in support of clearly identified technical tasks";
    }

    private String buildMetricsSummary(AchievementMetric metric) {
        if (metric == null) {
            return ", showing practical skills demonstrated in a real project context.";
        }

        List<String> parts = new ArrayList<>();
        if (metric.getComplexityScore() != null) {
            parts.add(metricTone(metric.getComplexityScore()) + " complexity");
        }
        if (metric.getImpactScore() != null) {
            parts.add(metricTone(metric.getImpactScore()) + " impact");
        }
        if (metric.getDurationDays() != null && metric.getDurationDays() > 0) {
            parts.add("a duration of " + metric.getDurationDays() + " days");
        }

        if (parts.isEmpty()) {
            return ", with delivery focused on quality and measurable results.";
        }
        return ", in a context defined by " + joinEnglishList(parts) + ".";
    }

    private String metricTone(Integer score) {
        if (score == null) {
            return "solid";
        }
        if (score >= 8) {
            return "high";
        }
        if (score >= 6) {
            return "strong";
        }
        return "moderate";
    }

    private int contributionScore(ContributionLevel level) {
        if (level == null) {
            return 0;
        }
        return switch (level) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private String joinEnglishList(List<String> items) {
        List<String> cleaned = items.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (cleaned.isEmpty()) {
            return "";
        }
        if (cleaned.size() == 1) {
            return cleaned.get(0);
        }
        if (cleaned.size() == 2) {
            return cleaned.get(0) + " and " + cleaned.get(1);
        }
        return String.join(", ", cleaned.subList(0, cleaned.size() - 1)) + " and " + cleaned.get(cleaned.size() - 1);
    }

    private Map<Long, SkillDTO> loadSkillCatalog(List<AchievementSkill> achievementSkills) {
        try {
            Map<Long, SkillDTO> result = new HashMap<>();
            for (SkillDTO skill : skillClient.getAllSkills()) {
                if (skill.getId() != null) {
                    result.putIfAbsent(skill.getId(), skill);
                }
            }
            return result;
        } catch (Exception ex) {
            return buildFallbackSkillCatalog(achievementSkills);
        }
    }

    private Map<Long, SkillDTO> buildFallbackSkillCatalog(List<AchievementSkill> achievementSkills) {
        Map<Long, SkillDTO> result = new HashMap<>();
        for (AchievementSkill achievementSkill : achievementSkills) {
            Long skillId = achievementSkill.getSkillId();
            if (skillId == null) {
                continue;
            }
            result.putIfAbsent(skillId, SkillDTO.builder()
                    .id(skillId)
                    .name("Skill #" + skillId)
                    .build());
        }
        return result;
    }

    private String resolveSkillName(Long skillId, Map<Long, SkillDTO> skillsById) {
        if (skillId == null) {
            return "Unknown skill";
        }
        SkillDTO skill = skillsById.get(skillId);
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) {
            return "Skill #" + skillId;
        }
        return skill.getName();
    }

    private String sanitizeModelOutput(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
