package tn.esprit.gestionskills.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.skills;

import java.util.List;

public interface IskillsInterface {
    skills addSkill(skills s);
    skills updateSkill(skills s);
    skills getSkillById(Long id);
    List<skills> getAllSkills();
    void deleteSkill(Long id);
    Page<skills> search(String q, SkillLevel level, Pageable pageable);

    int getScore(Long skillId);
    List<tn.esprit.gestionskills.dto.SkillScoreDto> getScoreboard(int size);

    String getBadge(Long skillId);

}
