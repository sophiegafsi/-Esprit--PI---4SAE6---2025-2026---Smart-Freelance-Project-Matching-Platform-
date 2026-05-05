package tn.esprit.gestionskills.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.SkillProof;
import tn.esprit.gestionskills.Services.SkillProofService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/proofs")
@RequiredArgsConstructor
public class SkillProofController {

    private final SkillProofService proofService;

    @PostMapping("/skill/{skillId}")
    public SkillProof addProofToSkill(@PathVariable Long skillId, @RequestBody SkillProof proof) {
        return proofService.addProofToSkill(skillId, proof);
    }

    // ✅ Upload + expiresAt optionnel (YYYY-MM-DD)
    @PostMapping(
            value = "/skill/{skillId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public SkillProof uploadProofToSkill(
            @PathVariable Long skillId,
            @RequestParam("title") String title,
            @RequestParam("type") ProofType type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiresAt", required = false) String expiresAt
    ) {
        LocalDate exp = (expiresAt == null || expiresAt.isBlank()) ? null : LocalDate.parse(expiresAt);
        return proofService.uploadProofToSkill(skillId, title, type, file, exp);
    }

    @PutMapping("/update")
    public SkillProof updateProof(@RequestBody SkillProof proof) {
        return proofService.updateProof(proof);
    }

    @GetMapping("/{id}")
    public SkillProof getProofById(@PathVariable Long id) {
        return proofService.getProofById(id);
    }

    @GetMapping("/getall")
    public java.util.List<SkillProof> getAllProofs() {
        return proofService.getAllProofs();
    }

    // ✅ Filtre state: VALID | EXPIRED | EXPIRING_SOON
    @GetMapping("/skill/{skillId}")
    public java.util.List<SkillProof> getProofsBySkill(
            @PathVariable Long skillId,
            @RequestParam(value = "state", required = false) ProofState state
    ) {
        return proofService.getProofsBySkill(skillId, state);
    }

    @DeleteMapping("/{id}")
    public void deleteProof(@PathVariable Long id) {
        proofService.deleteProof(id);
    }
}