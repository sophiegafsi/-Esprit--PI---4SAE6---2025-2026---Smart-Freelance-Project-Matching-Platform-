package tn.esprit.GestionPortfolio.Services;

import tn.esprit.GestionPortfolio.Entities.Achievement;

import java.util.List;

public interface IAchievementService {
    Achievement addAchievement(Achievement achievement);
    Achievement updateAchievement(Achievement achievement);
    void deleteAchievement(Long id);
    Achievement getAchievementById(Long id);
    List<Achievement> getAllAchievements();
    List<Achievement> getAchievementsByFreelancerId(String freelancerId);
}