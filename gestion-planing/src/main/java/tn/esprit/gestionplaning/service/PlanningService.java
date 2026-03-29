package tn.esprit.gestionplaning.service;

import org.springframework.stereotype.Service;
import tn.esprit.gestionplaning.entities.Planning;
import tn.esprit.gestionplaning.exception.BusinessException;
import tn.esprit.gestionplaning.exception.ResourceNotFoundException;
import tn.esprit.gestionplaning.repository.PlanningRepository;

import java.util.List;

@Service
public class PlanningService {

    private final PlanningRepository planningRepository;

    public PlanningService(PlanningRepository planningRepository) {
        this.planningRepository = planningRepository;
    }

    public List<Planning> getAllPlannings() {
        return planningRepository.findAll();
    }

    public Planning getPlanningById(Long id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planning not found with id: " + id));
    }

    public Planning addPlanning(Planning planning) {
        validatePlanningDates(planning);
        return planningRepository.save(planning);
    }

    public Planning updatePlanning(Long id, Planning planningDetails) {
        Planning planning = getPlanningById(id);

        validatePlanningDates(planningDetails);

        planning.setTitle(planningDetails.getTitle());
        planning.setDescription(planningDetails.getDescription());
        planning.setStartDate(planningDetails.getStartDate());
        planning.setEndDate(planningDetails.getEndDate());
        planning.setStatus(planningDetails.getStatus());

        return planningRepository.save(planning);
    }

    public void deletePlanning(Long id) {
        Planning planning = getPlanningById(id);
        planningRepository.delete(planning);
    }

    private void validatePlanningDates(Planning planning) {
        if (planning.getStartDate() != null && planning.getEndDate() != null
                && planning.getEndDate().isBefore(planning.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }
    }
}