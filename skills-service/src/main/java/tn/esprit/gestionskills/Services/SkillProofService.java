package tn.esprit.gestionskills.Services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.SkillProof;

import java.time.LocalDate;
import java.util.List;

public interface SkillProofService {

    SkillProof addProofToSkill(Long skillId, SkillProof proof);

    // ✅ Nouvelle méthode avec expiresAt
    SkillProof uploadProofToSkill(Long skillId, String title, ProofType type, MultipartFile file, LocalDate expiresAt);

    SkillProof updateProof(SkillProof proof);

    SkillProof getProofById(Long id);

    List<SkillProof> getAllProofs();

    // ✅ Optionnel : state (si tu l'as ajouté)
    List<SkillProof> getProofsBySkill(Long skillId, ProofState state);

    void deleteProof(Long id);
}