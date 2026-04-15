package tn.esprit.GestionPortfolio.Controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Services.IAchievementService;

import java.util.List;

@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AchievementController {

    private final IAchievementService achievementService;

    @PostMapping
    public Achievement addAchievement(@Valid @RequestBody Achievement achievement) {
        return achievementService.addAchievement(achievement);
    }

    @PutMapping
    public Achievement updateAchievement(@Valid @RequestBody Achievement achievement) {
        return achievementService.updateAchievement(achievement);
    }

    @DeleteMapping("/{id}")
    public void deleteAchievement(@PathVariable Long id) {
        achievementService.deleteAchievement(id);
    }

    @GetMapping("/{id}")
    public Achievement getAchievementById(@PathVariable Long id) {
        return achievementService.getAchievementById(id);
    }

    @GetMapping
    public List<Achievement> getAllAchievements() {
        return achievementService.getAllAchievements();
    }

    @GetMapping("/freelancer/{freelancerId}")
    public List<Achievement> getAchievementsByFreelancerId(@PathVariable String freelancerId) {
        return achievementService.getAchievementsByFreelancerId(freelancerId);
    }
}