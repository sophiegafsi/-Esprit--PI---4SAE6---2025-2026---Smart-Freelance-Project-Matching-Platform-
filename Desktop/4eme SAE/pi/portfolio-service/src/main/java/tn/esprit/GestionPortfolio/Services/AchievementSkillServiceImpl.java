package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;
import tn.esprit.GestionPortfolio.Repository.AchievementSkillRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementSkillServiceImpl implements IAchievementSkillService {

    private final AchievementSkillRepository achievementSkillRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementMetricScoringService achievementMetricScoringService;

    @Override
    public AchievementSkill addAchievementSkill(Long achievementId, AchievementSkill achievementSkill) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement != null) {
            achievementSkill.setAchievement(achievement);
            AchievementSkill saved = achievementSkillRepository.save(achievementSkill);
            achievementMetricScoringService.syncMetricForAchievement(achievementId);
            return saved;
        }
        return null;
    }

    @Override
    public AchievementSkill updateAchievementSkill(AchievementSkill achievementSkill) {
        AchievementSkill saved = achievementSkillRepository.save(achievementSkill);
        Long achievementId = saved.getAchievement() == null ? null : saved.getAchievement().getId();
        if (achievementId != null) {
            achievementMetricScoringService.syncMetricForAchievement(achievementId);
        }
        return saved;
    }

    @Override
    public void deleteAchievementSkill(Long id) {
        achievementSkillRepository.findById(id).ifPresent(skill -> {
            Long achievementId = skill.getAchievement() == null ? null : skill.getAchievement().getId();
            achievementSkillRepository.delete(skill);
            if (achievementId != null) {
                achievementMetricScoringService.syncMetricForAchievement(achievementId);
            }
        });
    }

    @Override
    public AchievementSkill getAchievementSkillById(Long id) {
        return achievementSkillRepository.findById(id).orElse(null);
    }

    @Override
    public List<AchievementSkill> getSkillsByAchievementId(Long achievementId) {
        return achievementSkillRepository.findByAchievementId(achievementId);
    }
}
