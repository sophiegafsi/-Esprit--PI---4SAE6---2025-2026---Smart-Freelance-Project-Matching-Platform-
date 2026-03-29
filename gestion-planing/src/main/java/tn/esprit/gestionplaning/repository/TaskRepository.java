package tn.esprit.gestionplaning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionplaning.entities.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByPlanningId(Long planningId);
}