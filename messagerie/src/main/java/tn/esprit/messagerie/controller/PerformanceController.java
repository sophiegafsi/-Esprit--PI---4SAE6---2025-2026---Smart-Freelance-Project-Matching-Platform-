package tn.esprit.messagerie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.messagerie.DTO.PerformanceMetricsDTO;
import tn.esprit.messagerie.services.PerformanceService;
import tn.esprit.messagerie.services.PerformanceSeederService;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private PerformanceSeederService performanceSeederService;

    @GetMapping("/freelancer/{id}")
    public PerformanceMetricsDTO getFreelancerPerformance(@PathVariable Long id) {
        return performanceService.getFreelancerMetrics(id);
    }

    @GetMapping("/conversation/{id}")
    public PerformanceMetricsDTO getConversationPerformance(@PathVariable Long id) {
        return performanceService.getConversationMetrics(id);
    }

    @PostMapping("/seed-test-data")
    public String seedTestData(@RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long freelancerId) {
        if (clientId == null)
            clientId = 1L;
        if (freelancerId == null)
            freelancerId = 2L;
        performanceSeederService.seedRealisticConversation(clientId, freelancerId);
        return "Realistic test conversation seeded successfully between Client " + clientId + " and Freelancer "
                + freelancerId;
    }
}
