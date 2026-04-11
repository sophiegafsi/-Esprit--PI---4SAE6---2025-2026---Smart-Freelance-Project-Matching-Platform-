package tn.esprit.GestionPortfolio.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements IAchievementService {

    private final AchievementRepository achievementRepository;

    @Override
    public Achievement addAchievement(Achievement achievement) {
        return achievementRepository.save(achievement);
    }

    @Override
    public Achievement updateAchievement(Achievement achievement) {
        return achievementRepository.save(achievement);
    }

    @Override
    public void deleteAchievement(Long id) {
        achievementRepository.deleteById(id);
    }

    @Override
    public Achievement getAchievementById(Long id) {
        return achievementRepository.findById(id).orElse(null);
    }

    @Override
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    @Override
    public List<Achievement> getAchievementsByFreelancerId(Long freelancerId) {
        return achievementRepository.findByFreelancerId(freelancerId);
    }
}