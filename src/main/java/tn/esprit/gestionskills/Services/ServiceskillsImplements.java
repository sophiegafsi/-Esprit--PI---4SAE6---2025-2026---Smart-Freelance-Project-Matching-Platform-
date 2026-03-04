package tn.esprit.gestionskills.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.gestionskills.Entities.ProofType;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.skills;
import tn.esprit.gestionskills.Entities.skillsproof;
import tn.esprit.gestionskills.Repositories.skillsRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServiceskillsImplements implements IskillsInterface {

    private final skillsRepository skillsRepository;

    @Override
    public Page<skills> search(String q, SkillLevel level, Pageable pageable) {
        String query = (q == null) ? "" : q.trim();

        if (level == null) {
            return skillsRepository.findByNameContainingIgnoreCase(query, pageable);
        }
        return skillsRepository.findByNameContainingIgnoreCaseAndLevel(query, level, pageable);
    }

    @Override
    public skills addSkill(skills s) {
        String normalizedName = normalizeName(s.getName());
        s.setName(normalizedName);

        if (skillsRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalStateException("Skill already exists: " + normalizedName);
        }

        if (s.getProofs() != null) {
            for (skillsproof p : s.getProofs()) {
                p.setSkill(s);
            }
        }

        return skillsRepository.save(s);
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

    private boolean isValidProof(skillsproof p) {
        return p.getExpiresAt() == null || !p.getExpiresAt().isBefore(LocalDate.now());
    }

    private int computeScore(skills s) {
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
        skills s = skillsRepository.findById(skillId)
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
    public skills updateSkill(skills s) {
        if (s.getId() == null) {
            throw new IllegalArgumentException("ID skill obligatoire pour update");
        }

        skills existing = skillsRepository.findById(s.getId())
                .orElseThrow(() -> new IllegalArgumentException("Skill introuvable avec id=" + s.getId()));

        String newName = normalizeName(s.getName());

        if (!existing.getName().equalsIgnoreCase(newName)
                && skillsRepository.existsByNameIgnoreCase(newName)) {
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

            Set<skillsproof> incoming = new HashSet<>(s.getProofs());
            for (skillsproof p : incoming) {
                p.setSkill(existing);
                existing.getProofs().add(p);
            }
        }

        return skillsRepository.save(existing);
    }

    @Override
    public int getScore(Long skillId) {
        skills s = skillsRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill introuvable id=" + skillId));
        return computeScore(s);
    }

    @Override
    public List<tn.esprit.gestionskills.dto.SkillScoreDto> getScoreboard(int size) {
        List<skills> all = skillsRepository.findAll();
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
    public skills getSkillById(Long id) {
        return skillsRepository.findById(id).orElse(null);
    }

    @Override
    public List<skills> getAllSkills() {
        return skillsRepository.findAll();
    }

    @Override
    public void deleteSkill(Long id) {
        if (!skillsRepository.existsById(id)) {
            throw new IllegalArgumentException("Skill introuvable avec id=" + id);
        }
        skillsRepository.deleteById(id);
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        return name.trim();
    }
}