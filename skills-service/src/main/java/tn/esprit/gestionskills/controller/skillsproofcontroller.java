package tn.esprit.gestionskills.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.skillsproof;
import tn.esprit.gestionskills.Services.IskillsproofInterface;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/proofs")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class skillsproofcontroller {

    private final IskillsproofInterface proofService;

    @PostMapping("/skill/{skillId}")
    public skillsproof addProofToSkill(@PathVariable Long skillId, @RequestBody skillsproof proof) {
        return proofService.addProofToSkill(skillId, proof);
    }

    // ✅ Upload + expiresAt optionnel (YYYY-MM-DD)
    @PostMapping(
            value = "/skill/{skillId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public skillsproof uploadProofToSkill(
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
    public skillsproof updateProof(@RequestBody skillsproof proof) {
        return proofService.updateProof(proof);
    }

    @GetMapping("/{id}")
    public skillsproof getProofById(@PathVariable Long id) {
        return proofService.getProofById(id);
    }

    @GetMapping("/getall")
    public List<skillsproof> getAllProofs() {
        return proofService.getAllProofs();
    }

    // ✅ Filtre state: VALID | EXPIRED | EXPIRING_SOON
    @GetMapping("/skill/{skillId}")
    public List<skillsproof> getProofsBySkill(
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