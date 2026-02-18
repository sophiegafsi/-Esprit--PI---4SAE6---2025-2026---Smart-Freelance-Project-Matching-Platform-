package tn.esprit.gestionskills.Services;

import tn.esprit.gestionskills.Entities.skills;

import java.util.List;

public interface IskillsInterface {
    skills addSkill(skills s);
    skills updateSkill(skills s);
    skills getSkillById(Long id);
    List<skills> getAllSkills();
    void deleteSkill(Long id);
}
