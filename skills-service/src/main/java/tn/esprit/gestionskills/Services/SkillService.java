package tn.esprit.gestionskills.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.Skill;

import java.util.List;

public interface SkillService {
    Skill addSkill(Skill s);
    Skill updateSkill(Skill s);
    Skill getSkillById(Long id);
    java.util.List<Skill> getAllSkills();
    void deleteSkill(Long id);
    org.springframework.data.domain.Page<Skill> search(String q, tn.esprit.gestionskills.Entities.SkillLevel level, org.springframework.data.domain.Pageable pageable);

    int getScore(Long skillId);
    List<tn.esprit.gestionskills.dto.SkillScoreDto> getScoreboard(int size);

    String getBadge(Long skillId);
}
