package tn.esprit.gestionskills.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionskills.Entities.skills;
import tn.esprit.gestionskills.Entities.skillsproof;
import tn.esprit.gestionskills.Repositories.skillsRepository;
import tn.esprit.gestionskills.Repositories.skillsproofRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceskillsproofImplements implements IskillsproofInterface {

    private final skillsproofRepository skillsproofRepository;
    private final skillsRepository skillsRepository;

    @Override
    public skillsproof addProofToSkill(Long skillId, skillsproof proof) {
        skills s = skillsRepository.findById(skillId).orElse(null);
        if (s == null) return null;

        proof.setSkill(s);
        return skillsproofRepository.save(proof);
    }

    @Override
    public skillsproof updateProof(skillsproof proof) {
        return skillsproofRepository.save(proof);
    }

    @Override
    public skillsproof getProofById(Long id) {
        return skillsproofRepository.findById(id).orElse(null);
    }

    @Override
    public List<skillsproof> getAllProofs() {
        return skillsproofRepository.findAll();
    }

    @Override
    public List<skillsproof> getProofsBySkill(Long skillId) {
        return skillsproofRepository.findBySkill_Id(skillId);
    }

    @Override
    public void deleteProof(Long id) {
        skillsproofRepository.deleteById(id);
    }
}
