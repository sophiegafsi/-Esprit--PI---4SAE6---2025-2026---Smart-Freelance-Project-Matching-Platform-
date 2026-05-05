package tn.esprit.gestionskills.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Page<Skill> findByUserIdAndNameContainingIgnoreCase(String userId, String q, Pageable pageable);

    Page<Skill> findByUserIdAndNameContainingIgnoreCaseAndLevel(String userId, String q, SkillLevel level, Pageable pageable);

    boolean existsByUserIdAndNameIgnoreCaseAndLevel(String userId, String name, SkillLevel level);

    boolean existsByUserIdAndNameIgnoreCase(String userId, String name);

    java.util.List<Skill> findByUserId(String userId);

    Page<Skill> findByNameContainingIgnoreCase(String q, Pageable pageable);

    Page<Skill> findByNameContainingIgnoreCaseAndLevel(String q, SkillLevel level, Pageable pageable);

    boolean existsByNameIgnoreCaseAndLevel(String name, SkillLevel level);

    boolean existsByNameIgnoreCase(String name);
}
