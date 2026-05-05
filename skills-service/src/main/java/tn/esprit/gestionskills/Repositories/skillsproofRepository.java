package tn.esprit.gestionskills.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionskills.Entities.skillsproof;

import java.util.List;

public interface skillsproofRepository extends JpaRepository<skillsproof, Long> {
    List<skillsproof> findBySkill_Id(Long skillId);
}
