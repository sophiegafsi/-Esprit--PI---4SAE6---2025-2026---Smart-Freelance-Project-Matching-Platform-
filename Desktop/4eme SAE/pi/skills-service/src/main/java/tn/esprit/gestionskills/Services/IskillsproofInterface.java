package tn.esprit.gestionskills.Services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.skillsproof;

import java.time.LocalDate;
import java.util.List;

public interface IskillsproofInterface {

    skillsproof addProofToSkill(Long skillId, skillsproof proof);


    // ✅ Nouvelle méthode avec expiresAt
    skillsproof uploadProofToSkill(Long skillId, String title, ProofType type, MultipartFile file, LocalDate expiresAt);

    skillsproof updateProof(skillsproof proof);

    skillsproof getProofById(Long id);

    List<skillsproof> getAllProofs();

    // ✅ Optionnel : state (si tu l'as ajouté)
    List<skillsproof> getProofsBySkill(Long skillId, ProofState state);

    void deleteProof(Long id);
}