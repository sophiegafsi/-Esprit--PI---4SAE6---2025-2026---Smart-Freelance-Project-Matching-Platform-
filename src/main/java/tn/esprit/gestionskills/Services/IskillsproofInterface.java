package tn.esprit.gestionskills.Services;

import tn.esprit.gestionskills.Entities.skillsproof;

import java.util.List;

public interface IskillsproofInterface {

    skillsproof addProofToSkill(Long skillId, skillsproof proof);

    skillsproof updateProof(skillsproof proof);

    skillsproof getProofById(Long id);

    List<skillsproof> getAllProofs();

    List<skillsproof> getProofsBySkill(Long skillId);

    void deleteProof(Long id);
}
