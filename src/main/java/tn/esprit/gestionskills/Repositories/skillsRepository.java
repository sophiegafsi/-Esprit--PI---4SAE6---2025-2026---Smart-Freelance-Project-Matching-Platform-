package tn.esprit.gestionskills.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.skills;

@Repository
public interface  skillsRepository extends JpaRepository<skills, Long> {

    Page<skills> findByNameContainingIgnoreCase(String q, Pageable pageable);

    Page<skills> findByNameContainingIgnoreCaseAndLevel(String q, SkillLevel level, Pageable pageable);

    boolean existsByNameIgnoreCaseAndLevel(String name, SkillLevel level);

    boolean existsByNameIgnoreCase(String name);
}
