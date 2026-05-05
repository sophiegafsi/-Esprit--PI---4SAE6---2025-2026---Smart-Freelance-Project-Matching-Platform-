package tn.esprit.gestionskills.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.Skill;
import tn.esprit.gestionskills.Entities.SkillProof;
import tn.esprit.gestionskills.Repositories.SkillRepository;
import tn.esprit.gestionskills.Repositories.SkillProofRepository;

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
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class SkillProofServiceImpl implements SkillProofService {

    private final SkillProofRepository skillProofRepository;
    private final SkillRepository skillRepository;

    private final Path uploadRoot = Paths.get("uploads");
    private static final int EXPIRING_SOON_DAYS = 30;

    private String getCurrentUserId() {
        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        if (context == null) return "anonymousUser";
        var auth = context.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymousUser";
        }
        return auth.getName();
    }

    // -------------------------
    // Helpers état expiration
    // -------------------------
    private boolean isExpired(SkillProof p) {
        return p.getExpiresAt() != null && p.getExpiresAt().isBefore(LocalDate.now());
    }

    private boolean isValid(SkillProof p) {
        return p.getExpiresAt() == null || !p.getExpiresAt().isBefore(LocalDate.now());
    }

    private boolean isExpiringSoon(SkillProof p) {
        if (p.getExpiresAt() == null) return false;
        LocalDate today = LocalDate.now();
        return (p.getExpiresAt().isAfter(today) || p.getExpiresAt().isEqual(today))
                && !p.getExpiresAt().isAfter(today.plusDays(EXPIRING_SOON_DAYS));
    }

    // -------------------------
    // CRUD / Métiers
    // -------------------------

    @Override
    @org.springframework.transaction.annotation.Transactional
    public SkillProof addProofToSkill(Long skillId, SkillProof proof) {
        Skill s = skillRepository.findById(skillId).orElse(null);
        if (s == null) return null;
        if (s.getUserId() != null && !s.getUserId().equals(getCurrentUserId())) {
             throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }

        proof.setSkill(s);
        return skillProofRepository.save(proof);
    }

    // ✅ Nouvelle signature avec expiresAt
    @Override
    @org.springframework.transaction.annotation.Transactional
    public SkillProof uploadProofToSkill(Long skillId, String title, ProofType type, MultipartFile file, LocalDate expiresAt) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Fichier non image");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Image trop grande (max 2MB)");
        }

        Skill s = skillRepository.findById(skillId).orElse(null);
        if (s == null) {
            throw new IllegalArgumentException("Skill introuvable: " + skillId);
        }
        if (s.getUserId() != null && !s.getUserId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }

        try {
            Files.createDirectories(uploadRoot);

            String original = file.getOriginalFilename() == null ? "proof" : file.getOriginalFilename();
            String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = UUID.randomUUID() + "_" + safeName;

            Path target = uploadRoot.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            SkillProof proof = new SkillProof();
            proof.setTitle(title);
            proof.setType(type);
            proof.setFileUrl("/uploads/" + filename);
            proof.setExpiresAt(expiresAt); // ✅ NOUVEAU
            proof.setSkill(s);

            return skillProofRepository.save(proof);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur upload fichier", e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public SkillProof updateProof(SkillProof proof) {
        // IMPORTANT: ne pas perdre la relation skill (sinon skill_id devient NULL)
        SkillProof existing = skillProofRepository.findById(proof.getId()).orElse(null);
        if (existing == null) return null;
        if (existing.getSkill().getUserId() != null && !existing.getSkill().getUserId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }

        existing.setTitle(proof.getTitle());
        existing.setType(proof.getType());
        existing.setFileUrl(proof.getFileUrl());
        existing.setExpiresAt(proof.getExpiresAt()); // ✅ NOUVEAU

        return skillProofRepository.save(existing);
    }

    @Override
    public SkillProof getProofById(Long id) {
        return skillProofRepository.findById(id).orElse(null);
    }

    @Override
    public java.util.List<SkillProof> getAllProofs() {
        return skillProofRepository.findAll();
    }


    // ✅ Nouvelle méthode avec filtre par état
    @Override
    public java.util.List<SkillProof> getProofsBySkill(Long skillId, ProofState state) {
        java.util.List<SkillProof> list = skillProofRepository.findBySkill_Id(skillId);
        if (state == null) return list;

        return switch (state) {
            case VALID -> list.stream().filter(this::isValid).toList();
            case EXPIRED -> list.stream().filter(this::isExpired).toList();
            case EXPIRING_SOON -> list.stream().filter(this::isExpiringSoon).toList();
        };
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteProof(Long id) {
        SkillProof existing = skillProofRepository.findById(id).orElse(null);
        if (existing != null && existing.getSkill().getUserId() != null && !existing.getSkill().getUserId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }
        skillProofRepository.deleteById(id);
    }
}