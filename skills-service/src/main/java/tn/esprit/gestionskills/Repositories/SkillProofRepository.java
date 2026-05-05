package tn.esprit.gestionskills.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionskills.Entities.SkillProof;

import java.util.List;

public interface SkillProofRepository extends JpaRepository<SkillProof, Long> {
    java.util.List<SkillProof> findBySkill_Id(Long skillId);
}
