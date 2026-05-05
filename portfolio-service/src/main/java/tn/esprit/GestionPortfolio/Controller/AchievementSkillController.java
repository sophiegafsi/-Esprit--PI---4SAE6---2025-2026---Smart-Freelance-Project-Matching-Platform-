package tn.esprit.GestionPortfolio.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;
import tn.esprit.GestionPortfolio.Services.IAchievementSkillService;

import java.util.List;

@RestController
@RequestMapping("/achievement-skills")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AchievementSkillController {

    private final IAchievementSkillService achievementSkillService;

    @PostMapping("/achievement/{achievementId}")
    public AchievementSkill addAchievementSkill(@PathVariable Long achievementId,
                                                @RequestBody AchievementSkill achievementSkill) {
        return achievementSkillService.addAchievementSkill(achievementId, achievementSkill);
    }

    @PutMapping
    public AchievementSkill updateAchievementSkill(@RequestBody AchievementSkill achievementSkill) {
        return achievementSkillService.updateAchievementSkill(achievementSkill);
    }

    @DeleteMapping("/{id}")
    public void deleteAchievementSkill(@PathVariable Long id) {
        achievementSkillService.deleteAchievementSkill(id);
    }

    @GetMapping("/{id}")
    public AchievementSkill getAchievementSkillById(@PathVariable Long id) {
        return achievementSkillService.getAchievementSkillById(id);
    }

    @GetMapping("/achievement/{achievementId}")
    public List<AchievementSkill> getSkillsByAchievementId(@PathVariable Long achievementId) {
        return achievementSkillService.getSkillsByAchievementId(achievementId);
    }
}