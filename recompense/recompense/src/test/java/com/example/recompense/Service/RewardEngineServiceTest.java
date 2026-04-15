package com.example.recompense.Service;

import com.example.recompense.DTO.RewardDashboardDTO;
import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.DTO.RewardProcessingResponse;
import com.example.recompense.Entity.Badge;
import com.example.recompense.Entity.FreelancerRewardProfile;
import com.example.recompense.Entity.RewardHistory;
import com.example.recompense.Entity.UserBadge;
import com.example.recompense.Entity.UserPoints;
import com.example.recompense.Repository.FreelancerRewardProfileRepository;
import com.example.recompense.Repository.RewardHistoryRepository;
import com.example.recompense.Repository.UserBadgeRepository;
import com.example.recompense.Repository.UserPointsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardEngineServiceTest {

    @Mock
    private BadgeService badgeService;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private UserPointsRepository userPointsRepository;

    @Mock
    private FreelancerRewardProfileRepository profileRepository;

    @Mock
    private RewardHistoryRepository rewardHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private RewardEngineService rewardEngineService;

    @Captor
    private ArgumentCaptor<RewardHistory> rewardHistoryCaptor;

    @Captor
    private ArgumentCaptor<FreelancerRewardProfile> profileCaptor;

    @Test
    void processEvaluation_shouldAwardScoreAndPointsBadgesAndUpgradeLevel() {
        RewardEvaluationSyncRequest request = baseRequest();
        request.setAverageScore(4.7);
        request.setTotalPoints(120);
        request.setTotalEvaluations(3);
        request.setPositiveEvaluations(2);
        request.setCompletedProjects(3);

        Badge expert = badge("Expert", "AVERAGE_SCORE", 4.5, false);
        Badge bronze = badge("Bronze", "POINTS", 100.0, false);

        when(profileRepository.findByUserEmail(request.getFreelancerEmail())).thenReturn(Optional.empty());
        when(userPointsRepository.findOptionalByUserEmail(request.getFreelancerEmail())).thenReturn(Optional.empty());
        when(badgeService.findBestAutoBadge("AVERAGE_SCORE", 4.7)).thenReturn(expert);
        when(badgeService.findBestAutoBadge("POINTS", 120.0)).thenReturn(bronze);
        when(userBadgeRepository.existsByUserNameAndBadgeAndActiveTrue(request.getFreelancerEmail(), expert)).thenReturn(false);
        when(userBadgeRepository.existsByUserNameAndBadgeAndActiveTrue(request.getFreelancerEmail(), bronze)).thenReturn(false);
        when(userPointsRepository.save(any(UserPoints.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userBadgeRepository.save(any(UserBadge.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.save(any(FreelancerRewardProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rewardHistoryRepository.save(any(RewardHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RewardProcessingResponse response = rewardEngineService.processEvaluation(request);

        assertThat(response.getFreelancerEmail()).isEqualTo("freelancer@test.com");
        assertThat(response.getCurrentScoreBadge()).isEqualTo("Expert");
        assertThat(response.getCurrentPointsBadge()).isEqualTo("Bronze");
        assertThat(response.getCurrentLevel()).isEqualTo("Niveau 2 - Trusted Freelancer");
        assertThat(response.getTotalPoints()).isEqualTo(120);

        verify(userPointsRepository).save(any(UserPoints.class));
        verify(userBadgeRepository, times(2)).save(any(UserBadge.class));
        verify(profileRepository).save(profileCaptor.capture());
        verify(rewardHistoryRepository, times(3)).save(rewardHistoryCaptor.capture());
        verify(notificationService, times(3)).createAndBroadcast(eq("freelancer@test.com"), anyString());
        verify(emailService, times(2)).sendBadgeEmail(
                eq("freelancer@test.com"),
                anyString(),
                anyString(),
                eq(null),
                nullable(String.class)
        );

        FreelancerRewardProfile savedProfile = profileCaptor.getValue();
        assertThat(savedProfile.getCurrentLevel()).isEqualTo("Niveau 2 - Trusted Freelancer");
        assertThat(savedProfile.getCurrentScoreBadge()).isEqualTo("Expert");
        assertThat(savedProfile.getCurrentPointsBadge()).isEqualTo("Bronze");
        assertThat(savedProfile.getTotalBadgesAwarded()).isEqualTo(2);

        assertThat(rewardHistoryCaptor.getAllValues())
                .extracting(RewardHistory::getActionType)
                .containsExactlyInAnyOrder("UPGRADED", "AWARDED", "AWARDED");
    }

    @Test
    void processEvaluation_shouldRevokeBadgesAndDowngradeLevelWhenMetricsDrop() {
        RewardEvaluationSyncRequest request = baseRequest();
        request.setCurrentScore(1);
        request.setAverageScore(2.0);
        request.setTotalPoints(10);
        request.setTotalEvaluations(5);
        request.setPositiveEvaluations(1);
        request.setCompletedProjects(1);

        FreelancerRewardProfile profile = new FreelancerRewardProfile();
        profile.setUserEmail(request.getFreelancerEmail());
        profile.setUserName("Amal");
        profile.setCurrentLevel("Niveau 3 - Top Performer");
        profile.setCurrentScoreBadge("Expert");
        profile.setCurrentPointsBadge("Bronze");

        UserBadge expertBadge = activeUserBadge("freelancer@test.com", "Expert");
        UserBadge bronzeBadge = activeUserBadge("freelancer@test.com", "Bronze");

        when(profileRepository.findByUserEmail(request.getFreelancerEmail())).thenReturn(Optional.of(profile));
        when(userPointsRepository.findOptionalByUserEmail(request.getFreelancerEmail())).thenReturn(Optional.of(new UserPoints()));
        when(badgeService.findBestAutoBadge("AVERAGE_SCORE", 2.0)).thenReturn(null);
        when(badgeService.findBestAutoBadge("POINTS", 10.0)).thenReturn(null);
        when(userBadgeRepository.findByUserNameAndActiveTrueOrderByDateAssignedDesc(request.getFreelancerEmail()))
                .thenReturn(List.of(expertBadge, bronzeBadge));
        when(userPointsRepository.save(any(UserPoints.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userBadgeRepository.save(any(UserBadge.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.save(any(FreelancerRewardProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rewardHistoryRepository.save(any(RewardHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RewardProcessingResponse response = rewardEngineService.processEvaluation(request);

        assertThat(response.getCurrentScoreBadge()).isNull();
        assertThat(response.getCurrentPointsBadge()).isNull();
        assertThat(response.getCurrentLevel()).isEqualTo("Niveau 1 - New Freelancer");

        verify(userBadgeRepository, times(2)).save(any(UserBadge.class));
        verify(rewardHistoryRepository, times(3)).save(rewardHistoryCaptor.capture());

        assertThat(expertBadge.isActive()).isFalse();
        assertThat(bronzeBadge.isActive()).isFalse();
        assertThat(rewardHistoryCaptor.getAllValues())
                .extracting(RewardHistory::getActionType)
                .containsExactlyInAnyOrder("DOWNGRADED", "REVOKED", "REVOKED");
    }

    @Test
    void recalculateStoredLevels_shouldUsePointsThresholdsForExistingProfiles() {
        FreelancerRewardProfile profileToUpgrade = new FreelancerRewardProfile();
        profileToUpgrade.setUserEmail("upgrade@test.com");
        profileToUpgrade.setAverageScore(4.1);
        profileToUpgrade.setPositiveEvaluations(5);
        profileToUpgrade.setCompletedProjects(1);
        profileToUpgrade.setTotalPoints(260);
        profileToUpgrade.setCurrentLevel("Niveau 1 - New Freelancer");

        FreelancerRewardProfile alreadyCorrect = new FreelancerRewardProfile();
        alreadyCorrect.setUserEmail("stable@test.com");
        alreadyCorrect.setAverageScore(3.2);
        alreadyCorrect.setPositiveEvaluations(2);
        alreadyCorrect.setCompletedProjects(3);
        alreadyCorrect.setTotalPoints(90);
        alreadyCorrect.setCurrentLevel("Niveau 2 - Trusted Freelancer");

        when(profileRepository.findAll()).thenReturn(List.of(profileToUpgrade, alreadyCorrect));
        when(profileRepository.save(any(FreelancerRewardProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int updatedCount = rewardEngineService.recalculateStoredLevels();

        assertThat(updatedCount).isEqualTo(1);
        assertThat(profileToUpgrade.getCurrentLevel()).isEqualTo("Niveau 3 - Top Performer");
        assertThat(alreadyCorrect.getCurrentLevel()).isEqualTo("Niveau 2 - Trusted Freelancer");
        verify(profileRepository).save(profileToUpgrade);
    }

    @Test
    void getDashboard_shouldAggregateTopFreelancersAndMonthlyProgress() {
        FreelancerRewardProfile topFreelancer = new FreelancerRewardProfile();
        topFreelancer.setUserEmail("top@test.com");
        topFreelancer.setUserName("Top");
        topFreelancer.setAverageScore(4.8);
        topFreelancer.setTotalPoints(300);
        topFreelancer.setPositiveEvaluations(6);
        topFreelancer.setCompletedProjects(5);
        topFreelancer.setCurrentLevel("Niveau 3 - Top Performer");
        topFreelancer.setCurrentScoreBadge("Expert");

        FreelancerRewardProfile noRewardFreelancer = new FreelancerRewardProfile();
        noRewardFreelancer.setUserEmail("none@test.com");
        noRewardFreelancer.setUserName("None");
        noRewardFreelancer.setAverageScore(3.0);
        noRewardFreelancer.setTotalPoints(30);
        noRewardFreelancer.setPositiveEvaluations(1);
        noRewardFreelancer.setCompletedProjects(1);

        RewardHistory expertAwardJanuary = rewardHistory("top@test.com", "Expert", "SCORE_BADGE", "AWARDED",
                LocalDateTime.of(2026, 1, 10, 10, 0));
        RewardHistory bronzeAwardJanuary = rewardHistory("top@test.com", "Bronze", "POINTS_BADGE", "AWARDED",
                LocalDateTime.of(2026, 1, 15, 10, 0));
        RewardHistory bronzeRevokeFebruary = rewardHistory("top@test.com", "Bronze", "POINTS_BADGE", "REVOKED",
                LocalDateTime.of(2026, 2, 5, 10, 0));
        RewardHistory expertAwardFebruary = rewardHistory("other@test.com", "Expert", "SCORE_BADGE", "AWARDED",
                LocalDateTime.of(2026, 2, 6, 10, 0));

        when(profileRepository.findAll()).thenReturn(List.of(noRewardFreelancer, topFreelancer));
        when(rewardHistoryRepository.findAllByOrderByEventDateDesc()).thenReturn(List.of(
                expertAwardFebruary,
                bronzeRevokeFebruary,
                bronzeAwardJanuary,
                expertAwardJanuary
        ));
        when(userBadgeRepository.countByActiveTrue()).thenReturn(2L);

        RewardDashboardDTO dashboard = rewardEngineService.getDashboard();

        assertThat(dashboard.getTotalBadgesAssigned()).isEqualTo(3);
        assertThat(dashboard.getActiveBadges()).isEqualTo(2);
        assertThat(dashboard.getMostFrequentBadge()).isEqualTo("Expert");
        assertThat(dashboard.getFreelancersWithoutRewardCount()).isEqualTo(1);
        assertThat(dashboard.getFreelancersWithoutReward()).containsExactly("none@test.com");
        assertThat(dashboard.getTopFreelancers()).hasSize(2);
        assertThat(dashboard.getTopFreelancers().get(0).getUserEmail()).isEqualTo("top@test.com");
        assertThat(dashboard.getMonthlyProgress()).hasSize(2);
        assertThat(dashboard.getMonthlyProgress().get(0).getMonth()).isEqualTo("2026-01");
        assertThat(dashboard.getMonthlyProgress().get(0).getAwardedCount()).isEqualTo(2);
        assertThat(dashboard.getMonthlyProgress().get(0).getRevokedCount()).isEqualTo(0);
        assertThat(dashboard.getMonthlyProgress().get(1).getMonth()).isEqualTo("2026-02");
        assertThat(dashboard.getMonthlyProgress().get(1).getAwardedCount()).isEqualTo(1);
        assertThat(dashboard.getMonthlyProgress().get(1).getRevokedCount()).isEqualTo(1);
    }

    @Test
    void generateCertificate_shouldRejectHistoryWithoutRewardName() {
        RewardHistory history = new RewardHistory();
        history.setId(7L);
        history.setUserEmail("amal@test.com");
        history.setRewardName(" ");

        when(rewardHistoryRepository.findById(7L)).thenReturn(Optional.of(history));

        assertThatThrownBy(() -> rewardEngineService.generateCertificate(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing the reward name");
    }

    @Test
    void resendRewardEmail_shouldUseProvidedRecipientAndAttachCertificate() {
        RewardHistory history = rewardHistory(
                "amal@test.com",
                "Expert",
                "SCORE_BADGE",
                "AWARDED",
                LocalDateTime.of(2026, 4, 14, 16, 10)
        );
        history.setId(1L);
        history.setUserName("Amal");
        history.setAverageScoreSnapshot(5.0);
        history.setTotalPointsSnapshot(50);
        history.setScoreSnapshot(5);
        history.setCertificateGenerated(true);

        FreelancerRewardProfile profile = new FreelancerRewardProfile();
        profile.setUserEmail("amal@test.com");
        profile.setUserName("Amal");
        profile.setCurrentLevel("Niveau 2 - Trusted Freelancer");

        byte[] pdf = "%PDF-test".getBytes();

        when(rewardHistoryRepository.findById(1L)).thenReturn(Optional.of(history));
        when(profileRepository.findByUserEmail("amal@test.com")).thenReturn(Optional.of(profile));
        when(certificateService.generateCertificate(history)).thenReturn(pdf);

        rewardEngineService.resendRewardEmail(1L, "dest@test.com");

        verify(emailService).sendBadgeEmail(
                eq("dest@test.com"),
                eq("Your reward certificate: Expert"),
                org.mockito.ArgumentMatchers.contains("Level: Niveau 2 - Trusted Freelancer"),
                eq(pdf),
                org.mockito.ArgumentMatchers.contains("certificate-amal_test.com-Expert")
        );
    }

    private RewardEvaluationSyncRequest baseRequest() {
        RewardEvaluationSyncRequest request = new RewardEvaluationSyncRequest();
        request.setEvaluationId(12L);
        request.setFreelancerEmail("freelancer@test.com");
        request.setFreelancerName("Amal");
        request.setProjectName("Projet Test");
        request.setCurrentScore(5);
        request.setAverageScore(5.0);
        request.setTotalPoints(50);
        request.setTotalEvaluations(1);
        request.setPositiveEvaluations(1);
        request.setCompletedProjects(1);
        request.setEvaluatedAt(LocalDateTime.of(2026, 4, 14, 16, 10));
        return request;
    }

    private Badge badge(String name, String conditionType, Double threshold, boolean certificateEligible) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setConditionType(conditionType);
        badge.setConditionValue(threshold);
        badge.setAutoAssignable(true);
        badge.setCertificateEligible(certificateEligible);
        badge.setIsActive(true);
        return badge;
    }

    private UserBadge activeUserBadge(String email, String badgeName) {
        UserBadge userBadge = new UserBadge();
        userBadge.setUserName(email);
        userBadge.setActive(true);
        userBadge.setDateAssigned(LocalDateTime.of(2026, 4, 10, 10, 0));

        Badge badge = new Badge();
        badge.setName(badgeName);
        userBadge.setBadge(badge);
        return userBadge;
    }

    private RewardHistory rewardHistory(String email,
                                        String rewardName,
                                        String rewardType,
                                        String actionType,
                                        LocalDateTime eventDate) {
        RewardHistory history = new RewardHistory();
        history.setUserEmail(email);
        history.setRewardName(rewardName);
        history.setRewardType(rewardType);
        history.setActionType(actionType);
        history.setEventDate(eventDate);
        return history;
    }
}
