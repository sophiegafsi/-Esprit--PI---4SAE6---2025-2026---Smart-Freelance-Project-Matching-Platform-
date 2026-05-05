package tn.esprit.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entities.Planning;
import tn.esprit.gestionplaning.PlanningDailyLoadResponse;
import tn.esprit.gestionplaning.PlanningEfficiencyResponse;
import tn.esprit.gestionplaning.PlanningProgressResponse;
import tn.esprit.gestionplaning.PlanningWeightedProgressResponse;
import tn.esprit.service.PlanningService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plannings")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping
    public List<Planning> getAllPlannings() {
        return planningService.getAllPlannings();
    }

    @GetMapping("/{id}")
    public Planning getPlanningById(@PathVariable Long id) {
        return planningService.getPlanningById(id);
    }

    @GetMapping("/search")
    public List<Planning> searchPlannings(@RequestParam String keyword) {
        return planningService.searchPlannings(keyword);
    }

    @PostMapping
    public Planning addPlanning(@Valid @RequestBody Planning planning) {
        return planningService.addPlanning(planning);
    }

    @PutMapping("/{id}")
    public Planning updatePlanning(@PathVariable Long id, @Valid @RequestBody Planning planning) {
        return planningService.updatePlanning(id, planning);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deletePlanning(@PathVariable Long id) {
        planningService.deletePlanning(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Planning deleted successfully");
        return response;
    }

    @GetMapping("/{id}/progress")
    public PlanningProgressResponse getPlanningProgress(@PathVariable Long id) {
        return planningService.getPlanningProgress(id);
    }

    @GetMapping("/{id}/weighted-progress")
    public PlanningWeightedProgressResponse getPlanningWeightedProgress(@PathVariable Long id) {
        return planningService.getPlanningWeightedProgress(id);
    }

    @GetMapping("/{id}/daily-load")
    public List<PlanningDailyLoadResponse> getPlanningDailyLoad(@PathVariable Long id) {
        return planningService.getPlanningDailyLoad(id);
    }

    @GetMapping("/{id}/efficiency")
    public PlanningEfficiencyResponse getPlanningEfficiency(@PathVariable Long id) {
        return planningService.getPlanningEfficiency(id);
    }
}