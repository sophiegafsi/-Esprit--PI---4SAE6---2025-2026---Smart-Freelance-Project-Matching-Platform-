package tn.esprit.gestionskills.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.gestionskills.Entities.Skill;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.SkillProof;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.ProofState;
import tn.esprit.gestionskills.Repositories.SkillRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    private String getCurrentUserId() {
        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        if (context == null) return "anonymousUser";
        var auth = context.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymousUser";
        }
        return auth.getName();
    }

    @Override
    public Page<Skill> search(String q, SkillLevel level, Pageable pageable) {
        String query = (q == null) ? "" : q.trim();
        String userId = getCurrentUserId();
        if (level == null) {
            return skillRepository.findByUserIdAndNameContainingIgnoreCase(userId, query, pageable);
        }
        return skillRepository.findByUserIdAndNameContainingIgnoreCaseAndLevel(userId, query, level, pageable);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Skill addSkill(Skill s) {
        String normalizedName = normalizeName(s.getName());
        s.setName(normalizedName);
        String userId = getCurrentUserId();
        s.setUserId(userId);

        if (skillRepository.existsByUserIdAndNameIgnoreCase(userId, normalizedName)) {
            throw new IllegalStateException("Skill already exists: " + normalizedName);
        }

        if (s.getProofs() != null) {
            for (SkillProof p : s.getProofs()) {
                p.setSkill(s);
            }
        }

        return skillRepository.save(s);
    }

    private int levelWeight(SkillLevel level) {
        if (level == null) return 0;
        return switch (level) {
            case BEGINNER -> 1;
            case INTERMEDIATE -> 2;
            case ADVANCED -> 3;
            case EXPERT -> 4;
        };
    }

    private boolean isValidProof(SkillProof p) {
        return p.getExpiresAt() == null || !p.getExpiresAt().isBefore(LocalDate.now());
    }

    private int computeScore(Skill s) {
        int years = (s.getYearsOfExperience() == null) ? 0 : s.getYearsOfExperience();
        int levelW = levelWeight(s.getLevel());

        int validProofsCount = 0;
        if (s.getProofs() != null) {
            validProofsCount = (int) s.getProofs()
                    .stream()
                    .filter(this::isValidProof)
                    .count();
        }

        return years * 2 + levelW * 5 + validProofsCount * 3;
    }

    // ✅ BADGE LOGIC
    @Override
    public String getBadge(Long skillId) {
        Skill s = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill introuvable id=" + skillId));

        int score = computeScore(s);

        boolean hasValidCertificate = false;

        if (s.getProofs() != null) {
            hasValidCertificate = s.getProofs().stream().anyMatch(p ->
                    (p.getType() == ProofType.CERTIFICATE || p.getType() == ProofType.DIPLOMA)
                            && isValidProof(p)
            );
        }

        if (score >= 70 && hasValidCertificate) return "CERTIFIED_EXPERT";
        if (score >= 70) return "EXPERT";
        if (score >= 40) return "ADVANCED";
        return "BEGINNER";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Skill updateSkill(Skill s) {
        if (s.getId() == null) {
            throw new IllegalArgumentException("ID skill obligatoire pour update");
        }

        Skill existing = skillRepository.findById(s.getId())
                .orElseThrow(() -> new IllegalArgumentException("Skill introuvable avec id=" + s.getId()));

        if (!existing.getUserId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }

        String newName = normalizeName(s.getName());

        if (!existing.getName().equalsIgnoreCase(newName)
                && skillRepository.existsByUserIdAndNameIgnoreCase(getCurrentUserId(), newName)) {
            throw new IllegalStateException("Skill already exists: " + newName);
        }

        existing.setName(newName);
        existing.setLevel(s.getLevel());
        existing.setYearsOfExperience(s.getYearsOfExperience());
        existing.setDescription(s.getDescription());

        if (s.getProofs() != null) {
            if (existing.getProofs() == null) {
                existing.setProofs(new HashSet<>());
            } else {
                existing.getProofs().clear();
            }

            Set<SkillProof> incoming = new HashSet<>(s.getProofs());
            for (SkillProof p : incoming) {
                p.setSkill(existing);
                existing.getProofs().add(p);
            }
        }

        return skillRepository.save(existing);
    }

    @Override
    public int getScore(Long skillId) {
        Skill s = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill introuvable id=" + skillId));
        return computeScore(s);
    }

    @Override
    public java.util.List<tn.esprit.gestionskills.dto.SkillScoreDto> getScoreboard(int size) {
        java.util.List<Skill> all = skillRepository.findAll();
        return all.stream()
                .map(s -> new tn.esprit.gestionskills.dto.SkillScoreDto(
                        s.getId(),
                        s.getName(),
                        computeScore(s)
                ))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(Math.max(size, 1))
                .toList();
    }

    @Override
    public Skill getSkillById(Long id) {
        Skill s = skillRepository.findById(id).orElse(null);
        if (s != null && s.getUserId() != null && !s.getUserId().equals(getCurrentUserId())) {
             throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }
        return s;
    }

    @Override
    public java.util.List<Skill> getAllSkills() {
        return skillRepository.findByUserId(getCurrentUserId());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteSkill(Long id) {
        Skill s = skillRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Skill introuvable avec id=" + id));
        if (s.getUserId() != null && !s.getUserId().equals(getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Operation non autorisee");
        }
        skillRepository.deleteById(id);
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        return name.trim();
    }
}