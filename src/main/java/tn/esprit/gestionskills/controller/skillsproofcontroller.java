package tn.esprit.gestionskills.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionskills.Entities.skillsproof;
import tn.esprit.gestionskills.Services.IskillsproofInterface;

import java.util.List;

@RestController
@RequestMapping("/proofs")
@RequiredArgsConstructor
public class skillsproofcontroller {

    private final IskillsproofInterface proofService;

    // ✅ Add proof to a specific skill
    @PostMapping("/skill/{skillId}")
    public skillsproof addProofToSkill(@PathVariable Long skillId,
                                       @RequestBody skillsproof proof) {
        return proofService.addProofToSkill(skillId, proof);
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

    // ✅ Get all proofs for a skill
    @GetMapping("/skill/{skillId}")
    public List<skillsproof> getProofsBySkill(@PathVariable Long skillId) {
        return proofService.getProofsBySkill(skillId);
    }

    @DeleteMapping("/{id}")
    public void deleteProof(@PathVariable Long id) {
        proofService.deleteProof(id);
    }
}
