package tn.esprit.gestionskills.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.skills;
import tn.esprit.gestionskills.Entities.skillsproof;
import tn.esprit.gestionskills.Repositories.skillsRepository;
import tn.esprit.gestionskills.Repositories.skillsproofRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceskillsproofImplements implements IskillsproofInterface {

    private final skillsproofRepository skillsproofRepository;
    private final skillsRepository skillsRepository;

    private final Path uploadRoot = Paths.get("uploads");
    private static final int EXPIRING_SOON_DAYS = 30;

    // -------------------------
    // Helpers état expiration
    // -------------------------
    private boolean isExpired(skillsproof p) {
        return p.getExpiresAt() != null && p.getExpiresAt().isBefore(LocalDate.now());
    }

    private boolean isValid(skillsproof p) {
        return p.getExpiresAt() == null || !p.getExpiresAt().isBefore(LocalDate.now());
    }

    private boolean isExpiringSoon(skillsproof p) {
        if (p.getExpiresAt() == null) return false;
        LocalDate today = LocalDate.now();
        return (p.getExpiresAt().isAfter(today) || p.getExpiresAt().isEqual(today))
                && !p.getExpiresAt().isAfter(today.plusDays(EXPIRING_SOON_DAYS));
    }

    // -------------------------
    // CRUD / Métiers
    // -------------------------

    @Override
    public skillsproof addProofToSkill(Long skillId, skillsproof proof) {
        skills s = skillsRepository.findById(skillId).orElse(null);
        if (s == null) return null;

        proof.setSkill(s);
        return skillsproofRepository.save(proof);
    }



    // ✅ Nouvelle signature avec expiresAt
    @Override
    public skillsproof uploadProofToSkill(Long skillId, String title, ProofType type, MultipartFile file, LocalDate expiresAt) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Fichier non image");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Image trop grande (max 2MB)");
        }

        skills s = skillsRepository.findById(skillId).orElse(null);
        if (s == null) {
            throw new IllegalArgumentException("Skill introuvable: " + skillId);
        }

        try {
            Files.createDirectories(uploadRoot);

            String original = file.getOriginalFilename() == null ? "proof" : file.getOriginalFilename();
            String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = UUID.randomUUID() + "_" + safeName;

            Path target = uploadRoot.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            skillsproof proof = new skillsproof();
            proof.setTitle(title);
            proof.setType(type);
            proof.setFileUrl("/uploads/" + filename);
            proof.setExpiresAt(expiresAt); // ✅ NOUVEAU
            proof.setSkill(s);

            return skillsproofRepository.save(proof);
        } catch (IOException e) {
            throw new RuntimeException("Erreur upload fichier", e);
        }
    }

    @Override
    public skillsproof updateProof(skillsproof proof) {
        // IMPORTANT: ne pas perdre la relation skill (sinon skill_id devient NULL)
        skillsproof existing = skillsproofRepository.findById(proof.getId()).orElse(null);
        if (existing == null) return null;

        existing.setTitle(proof.getTitle());
        existing.setType(proof.getType());
        existing.setFileUrl(proof.getFileUrl());
        existing.setExpiresAt(proof.getExpiresAt()); // ✅ NOUVEAU

        return skillsproofRepository.save(existing);
    }

    @Override
    public skillsproof getProofById(Long id) {
        return skillsproofRepository.findById(id).orElse(null);
    }

    @Override
    public List<skillsproof> getAllProofs() {
        return skillsproofRepository.findAll();
    }


    // ✅ Nouvelle méthode avec filtre par état
    @Override
    public List<skillsproof> getProofsBySkill(Long skillId, ProofState state) {
        List<skillsproof> list = skillsproofRepository.findBySkill_Id(skillId);
        if (state == null) return list;

        return switch (state) {
            case VALID -> list.stream().filter(this::isValid).toList();
            case EXPIRED -> list.stream().filter(this::isExpired).toList();
            case EXPIRING_SOON -> list.stream().filter(this::isExpiringSoon).toList();
        };
    }

    @Override
    public void deleteProof(Long id) {
        skillsproofRepository.deleteById(id);
    }
}