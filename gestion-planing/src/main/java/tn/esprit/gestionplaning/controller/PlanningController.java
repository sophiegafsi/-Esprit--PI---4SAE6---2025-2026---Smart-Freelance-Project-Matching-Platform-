package tn.esprit.gestionplaning.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionplaning.entities.Planning;
import tn.esprit.gestionplaning.service.PlanningService;

import java.util.List;

@RestController
@RequestMapping("/api/plannings")
@CrossOrigin(origins = "http://localhost:4200")
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

    @PostMapping
    public Planning addPlanning(@Valid @RequestBody Planning planning) {
        return planningService.addPlanning(planning);
    }

    @PutMapping("/{id}")
    public Planning updatePlanning(@PathVariable Long id, @Valid @RequestBody Planning planning) {
        return planningService.updatePlanning(id, planning);
    }

    @DeleteMapping("/{id}")
    public String deletePlanning(@PathVariable Long id) {
        planningService.deletePlanning(id);
        return "Planning deleted successfully";
    }
}