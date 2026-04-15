package com.example.recompense.Service;

import com.example.recompense.Entity.Badge;
import com.example.recompense.Repository.BadgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    void createBadge_shouldNormalizeAutomaticBadgeFields() {
        Badge badge = new Badge();
        badge.setName("Expert");
        badge.setCategory("score moyen");
        badge.setConditionType("score_moyen");
        badge.setConditionValue(4.5);
        badge.setCertificateEligible(true);
        badge.setPointsReward(null);
        when(badgeRepository.save(any(Badge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Badge saved = badgeService.createBadge(badge);

        assertThat(saved.getCategory()).isEqualTo("SCORE");
        assertThat(saved.getConditionType()).isEqualTo("AVERAGE_SCORE");
        assertThat(saved.getPointsReward()).isZero();
        assertThat(saved.getAutoAssignable()).isTrue();
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getCertificateEligible()).isTrue();
        verify(notificationService).createAndBroadcast("admin", "New badge available in catalog: Expert");
    }

    @Test
    void createBadge_shouldRejectAutomaticBadgeWithoutValidThreshold() {
        Badge badge = new Badge();
        badge.setName("Invalid");
        badge.setAutoAssignable(true);
        badge.setConditionType("POINTS");
        badge.setConditionValue(-1.0);

        assertThatThrownBy(() -> badgeService.createBadge(badge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("condition");
    }

    @Test
    void createBadge_shouldNormalizeManualBadgeByClearingCondition() {
        Badge badge = new Badge();
        badge.setName("Manual Badge");
        badge.setAutoAssignable(false);
        badge.setConditionType("POINTS");
        badge.setConditionValue(200.0);
        badge.setCategory("");
        when(badgeRepository.save(any(Badge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Badge saved = badgeService.createBadge(badge);

        assertThat(saved.getAutoAssignable()).isFalse();
        assertThat(saved.getConditionType()).isNull();
        assertThat(saved.getConditionValue()).isEqualTo(0.0);
        assertThat(saved.getCategory()).isEqualTo("CUSTOM");
    }

    @Test
    void findBestAutoBadge_shouldPickHighestEligibleThresholdWithLegacyLabels() {
        Badge beginner = autoBadge("Beginner", "SCORE_MOYEN", 2.5);
        Badge professional = autoBadge("Professional", "AVERAGE_SCORE", 3.5);
        Badge expert = autoBadge("Expert", "AVERAGE_SCORE", 4.5);
        Badge manual = autoBadge("Manual", "AVERAGE_SCORE", 4.8);
        manual.setAutoAssignable(false);

        when(badgeRepository.findByIsActiveTrue()).thenReturn(List.of(beginner, professional, expert, manual));

        Badge result = badgeService.findBestAutoBadge("score moyen", 4.6);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Expert");
    }

    @Test
    void toggleBadgeStatus_shouldPersistAndNotify() {
        Badge badge = new Badge();
        badge.setId(5L);
        badge.setName("Gold");
        badge.setIsActive(false);

        when(badgeRepository.findById(5L)).thenReturn(Optional.of(badge));
        when(badgeRepository.save(any(Badge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Badge updated = badgeService.toggleBadgeStatus(5L, true);

        assertThat(updated.getIsActive()).isTrue();
        verify(notificationService).createAndBroadcast("admin", "Badge Gold is now ACTIVE");
    }

    @Test
    void updateBadge_shouldNormalizeManualBadgeFields() {
        Badge existing = new Badge();
        existing.setId(3L);
        existing.setName("Legacy");
        existing.setIsActive(true);

        Badge update = new Badge();
        update.setName("Manual Update");
        update.setDescription("Updated");
        update.setAutoAssignable(false);
        update.setConditionType("POINTS");
        update.setConditionValue(300.0);
        update.setCategory("manuel");
        update.setCertificateEligible(null);
        update.setPointsReward(null);

        when(badgeRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(badgeRepository.save(any(Badge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Badge updated = badgeService.updateBadge(3L, update);

        assertThat(updated.getName()).isEqualTo("Manual Update");
        assertThat(updated.getConditionType()).isNull();
        assertThat(updated.getConditionValue()).isEqualTo(0.0);
        assertThat(updated.getCategory()).isEqualTo("CUSTOM");
        assertThat(updated.getCertificateEligible()).isFalse();
    }

    private Badge autoBadge(String name, String conditionType, double threshold) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setConditionType(conditionType);
        badge.setConditionValue(threshold);
        badge.setAutoAssignable(true);
        badge.setIsActive(true);
        return badge;
    }
}
