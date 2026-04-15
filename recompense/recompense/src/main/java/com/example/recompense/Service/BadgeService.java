package com.example.recompense.Service;

import com.example.recompense.Entity.Badge;
import com.example.recompense.Repository.BadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final NotificationService notificationService;

    public BadgeService(BadgeRepository badgeRepository,
                        NotificationService notificationService) {
        this.badgeRepository = badgeRepository;
        this.notificationService = notificationService;
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }

    public Optional<Badge> getBadgeById(Long id) {
        return badgeRepository.findById(id);
    }

    public List<Badge> getActiveBadges() {
        return badgeRepository.findByIsActiveTrue();
    }

    public List<Badge> getBadgesByCategory(String category) {
        return badgeRepository.findByCategory(category);
    }

    @Transactional
    public Badge createBadge(Badge badge) {
        normalizeBadge(badge);
        validateBadge(badge);
        Badge saved = badgeRepository.save(badge);
        notificationService.createAndBroadcast("admin", "New badge available in catalog: " + badge.getName());
        return saved;
    }

    @Transactional
    public Badge updateBadge(Long id, Badge badgeDetails) {
        normalizeBadge(badgeDetails);
        validateBadge(badgeDetails);

        Badge existingBadge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouve avec l'ID: " + id));

        existingBadge.setName(badgeDetails.getName());
        existingBadge.setDescription(badgeDetails.getDescription());
        existingBadge.setIcon(badgeDetails.getIcon());
        existingBadge.setConditionType(badgeDetails.getConditionType());
        existingBadge.setConditionValue(badgeDetails.getConditionValue());
        existingBadge.setCategory(badgeDetails.getCategory());
        existingBadge.setPointsReward(badgeDetails.getPointsReward());
        existingBadge.setAutoAssignable(badgeDetails.getAutoAssignable());
        existingBadge.setCertificateEligible(badgeDetails.getCertificateEligible());
        existingBadge.setIsActive(badgeDetails.getIsActive());

        return badgeRepository.save(existingBadge);
    }

    @Transactional
    public void deleteBadge(Long id) {
        if (!badgeRepository.existsById(id)) {
            throw new RuntimeException("Badge non trouve avec l'ID: " + id);
        }
        badgeRepository.deleteById(id);
    }

    @Transactional
    public Badge toggleBadgeStatus(Long id, boolean isActive) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouve avec l'ID: " + id));

        badge.setIsActive(isActive);
        Badge updated = badgeRepository.save(badge);
        notificationService.createAndBroadcast(
                "admin",
                "Badge " + badge.getName() + " is now " + (isActive ? "ACTIVE" : "INACTIVE")
        );
        return updated;
    }

    public boolean existsById(Long id) {
        return badgeRepository.existsById(id);
    }

    public Optional<Badge> findByNameIgnoreCase(String badgeName) {
        return badgeRepository.findByNameIgnoreCase(badgeName);
    }

    public Badge findBestAutoBadge(String conditionType, Double metric) {
        String normalizedConditionType = normalizeConditionType(conditionType);
        if (metric == null || normalizedConditionType == null) {
            return null;
        }

        return badgeRepository.findByIsActiveTrue()
                .stream()
                .filter(badge -> Boolean.TRUE.equals(badge.getAutoAssignable()))
                .filter(badge -> normalizedConditionType.equals(normalizeConditionType(badge.getConditionType())))
                .filter(badge -> badge.getConditionValue() != null)
                .filter(badge -> metric >= badge.getConditionValue())
                .max(java.util.Comparator.comparing(Badge::getConditionValue))
                .orElse(null);
    }

    private void validateBadge(Badge badge) {
        if (badge.getName() == null || badge.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du badge est obligatoire");
        }
        if (Boolean.TRUE.equals(badge.getAutoAssignable())) {
            if (badge.getConditionType() == null || badge.getConditionType().trim().isEmpty()) {
                throw new IllegalArgumentException("Le type de condition est obligatoire pour un badge automatique");
            }
            if (badge.getConditionValue() == null || badge.getConditionValue() < 0) {
                throw new IllegalArgumentException("La valeur de condition doit etre positive");
            }
        }
    }

    private void normalizeBadge(Badge badge) {
        if (badge == null) {
            return;
        }

        boolean autoAssignable = !Boolean.FALSE.equals(badge.getAutoAssignable());
        badge.setAutoAssignable(autoAssignable);
        badge.setCertificateEligible(Boolean.TRUE.equals(badge.getCertificateEligible()));
        badge.setIsActive(!Boolean.FALSE.equals(badge.getIsActive()));
        badge.setPointsReward(badge.getPointsReward() == null ? 0 : badge.getPointsReward());

        badge.setCategory(normalizeCategory(badge.getCategory(), autoAssignable, badge.getConditionType()));

        if (autoAssignable) {
            badge.setConditionType(normalizeConditionType(badge.getConditionType()));
        } else {
            badge.setConditionType(null);
            badge.setConditionValue(0.0);
        }
    }

    private String normalizeConditionType(String conditionType) {
        if (conditionType == null || conditionType.isBlank()) {
            return null;
        }

        String normalized = conditionType.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "AVERAGE_SCORE", "SCORE", "SCORE_MOYEN", "SCOREMOYEN" -> "AVERAGE_SCORE";
            case "POINTS", "POINTS_CUMULES", "POINTS_CUMULATIFS", "POINTS_CUMULATIF" -> "POINTS";
            case "LEVEL", "NIVEAU" -> "LEVEL";
            default -> normalized;
        };
    }

    private String normalizeCategory(String category, boolean autoAssignable, String conditionType) {
        if (category == null || category.isBlank()) {
            if (!autoAssignable) {
                return "CUSTOM";
            }
            String normalizedCondition = normalizeConditionType(conditionType);
            if ("AVERAGE_SCORE".equals(normalizedCondition)) {
                return "SCORE";
            }
            if ("POINTS".equals(normalizedCondition)) {
                return "POINTS";
            }
            if ("LEVEL".equals(normalizedCondition)) {
                return "LEVEL";
            }
            return "CUSTOM";
        }

        String normalized = category.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "AVERAGE_SCORE", "SCORE", "SCORE_MOYEN", "SCOREMOYEN" -> "SCORE";
            case "POINTS", "POINTS_CUMULES", "POINTS_CUMULATIFS", "POINTS_CUMULATIF" -> "POINTS";
            case "LEVEL", "NIVEAU" -> "LEVEL";
            case "CUSTOM", "MANUAL", "MANUEL" -> "CUSTOM";
            default -> normalized;
        };
    }
}
