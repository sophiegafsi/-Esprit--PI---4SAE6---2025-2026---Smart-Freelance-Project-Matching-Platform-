package tn.esprit.GestionPortfolio.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Services.IAchievementMetricService;

@RestController
@RequestMapping("/achievement-metrics")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AchievementMetricController {

    private final IAchievementMetricService achievementMetricService;

    @PostMapping("/achievement/{achievementId}")
    public AchievementMetric addAchievementMetric(@PathVariable Long achievementId,
                                                  @RequestBody AchievementMetric achievementMetric) {
        return achievementMetricService.addAchievementMetric(achievementId, achievementMetric);
    }

    @PutMapping
    public AchievementMetric updateAchievementMetric(@RequestBody AchievementMetric achievementMetric) {
        return achievementMetricService.updateAchievementMetric(achievementMetric);
    }

    @DeleteMapping("/{id}")
    public void deleteAchievementMetric(@PathVariable Long id) {
        achievementMetricService.deleteAchievementMetric(id);
    }

    @GetMapping("/achievement/{achievementId}")
    public AchievementMetric getMetricByAchievementId(@PathVariable Long achievementId) {
        return achievementMetricService.getMetricByAchievementId(achievementId);
    }
}