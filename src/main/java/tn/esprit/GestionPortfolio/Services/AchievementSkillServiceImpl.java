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

    @Override
    public AchievementSkill addAchievementSkill(Long achievementId, AchievementSkill achievementSkill) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement != null) {
            achievementSkill.setAchievement(achievement);
            return achievementSkillRepository.save(achievementSkill);
        }
        return null;
    }

    @Override
    public AchievementSkill updateAchievementSkill(AchievementSkill achievementSkill) {
        return achievementSkillRepository.save(achievementSkill);
    }

    @Override
    public void deleteAchievementSkill(Long id) {
        achievementSkillRepository.deleteById(id);
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