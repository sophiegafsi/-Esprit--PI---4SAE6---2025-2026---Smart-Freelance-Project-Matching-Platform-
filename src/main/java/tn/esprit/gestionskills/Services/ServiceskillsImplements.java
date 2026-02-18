package tn.esprit.gestionskills.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionskills.Entities.skills;
import tn.esprit.gestionskills.Repositories.skillsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceskillsImplements implements IskillsInterface {

    private final skillsRepository skillsRepository;

    @Override
    public skills addSkill(skills s) {
        return skillsRepository.save(s);
    }

    @Override
    public skills updateSkill(skills s) {
        return skillsRepository.save(s);
    }

    @Override
    public skills getSkillById(Long id) {
        return skillsRepository.findById(id).orElse(null);
    }

    @Override
    public List<skills> getAllSkills() {
        return skillsRepository.findAll();
    }

    @Override
    public void deleteSkill(Long id) {
        skillsRepository.deleteById(id);
    }
}
