package tn.esprit.GestionPortfolio.Services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.GestionPortfolio.DTO.AchievementMetricSuggestionResponse;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Entities.ContributionLevel;
import tn.esprit.GestionPortfolio.Repository.AchievementMetricRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementSkillRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementMetricScoringServiceTest {

    @Mock
    private AchievementSkillRepository achievementSkillRepository;

    @Mock
    private AchievementMetricRepository achievementMetricRepository;

    @InjectMocks
    private AchievementMetricScoringService scoringService;

    @Test
    @DisplayName("Should build complex suggestion score for multiple high-level skills")
    void testBuildSuggestionHighComplexity() {
        // Arrange
        Long achievementId = 1L;
        AchievementSkill skill1 = new AchievementSkill();
        skill1.setContributionLevel(ContributionLevel.HIGH);
        AchievementSkill skill2 = new AchievementSkill();
        skill2.setContributionLevel(ContributionLevel.HIGH);
        
        when(achievementSkillRepository.findByAchievementId(achievementId))
                .thenReturn(Arrays.asList(skill1, skill2));

        // Act
        AchievementMetricSuggestionResponse response = scoringService.buildSuggestion(achievementId);

        // Assert
        assertEquals(2, response.linkedSkillsCount());
        assertEquals(2, response.highContributionCount());
        // complexityScore = clamp(1.0 + (2 * 1.35) + (2 * 0.9) + (0 * 0.45)) = clamp(1.0 + 2.7 + 1.8) = clamp(5.5) = 6
        assertEquals(6, response.complexityScore());
        // impactScore = clamp(1.0 + (2 * 1.15) + (2 * 1.25) + 0 + 0) = clamp(1.0 + 2.3 + 2.5) = clamp(5.8) = 6
        assertEquals(6, response.impactScore());
    }

    @Test
    @DisplayName("Should return minimum score (1) for achievement with no skills")
    void testBuildSuggestionNoSkills() {
        when(achievementSkillRepository.findByAchievementId(1L)).thenReturn(Collections.emptyList());

        AchievementMetricSuggestionResponse response = scoringService.buildSuggestion(1L);

        assertEquals(0, response.linkedSkillsCount());
        assertEquals(1, response.complexityScore());
        assertEquals(1, response.impactScore());
    }

    @Test
    @DisplayName("Should clamp scores at maximum 10 for very complex achievements")
    void testBuildSuggestionClampedMax() {
        // Arrange
        Long achievementId = 1L;
        // Construct 10 high-level skills to exceed the max score of 10
        List<AchievementSkill> skills = Collections.nCopies(10, new AchievementSkill());
        skills.forEach(s -> s.setContributionLevel(ContributionLevel.HIGH));

        when(achievementSkillRepository.findByAchievementId(achievementId)).thenReturn(skills);

        // Act
        AchievementMetricSuggestionResponse response = scoringService.buildSuggestion(achievementId);

        // Assert
        assertEquals(10, response.complexityScore()); // Should be 10, not higher
        assertEquals(10, response.impactScore());
    }
}
