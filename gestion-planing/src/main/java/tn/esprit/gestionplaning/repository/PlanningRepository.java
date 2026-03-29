package tn.esprit.gestionplaning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionplaning.entities.Planning;

public interface PlanningRepository extends JpaRepository<Planning, Long> {
}