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
    private final ProfanityFilterService profanityFilterService;

    private String getCurrentUserId() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Achievement addAchievement(Achievement achievement) {
        achievement.setFreelancerId(getCurrentUserId());
        return achievementRepository.save(sanitizeAchievement(achievement));
    }

    @Override
    public Achievement updateAchievement(Achievement achievement) {
        Achievement existing = achievementRepository.findById(achievement.getId())
                .orElseThrow(() -> new IllegalArgumentException("Achievement introuvable"));
        if (existing.getFreelancerId() != null && !existing.getFreelancerId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }
        achievement.setFreelancerId(getCurrentUserId());
        return achievementRepository.save(sanitizeAchievement(achievement));
    }

    @Override
    public void deleteAchievement(Long id) {
        Achievement existing = achievementRepository.findById(id).orElse(null);
        if (existing != null && existing.getFreelancerId() != null && !existing.getFreelancerId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }
        achievementRepository.deleteById(id);
    }

    @Override
    public Achievement getAchievementById(Long id) {
        Achievement a = achievementRepository.findById(id).orElse(null);
        if (a != null && a.getFreelancerId() != null && !a.getFreelancerId().equals(getCurrentUserId())) {
             throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }
        return a;
    }

    @Override
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findByFreelancerId(getCurrentUserId());
    }

    @Override
    public List<Achievement> getAchievementsByFreelancerId(String freelancerId) {
        return achievementRepository.findByFreelancerId(freelancerId);
    }

    private Achievement sanitizeAchievement(Achievement achievement) {
        if (achievement == null) {
            return null;
        }

        achievement.setTitle(profanityFilterService.mask(achievement.getTitle()));
        achievement.setDescription(profanityFilterService.mask(achievement.getDescription()));
        return achievement;
    }
}
