package com.example.recompense.Service;

import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.DTO.RewardProcessingResponse;
import com.example.recompense.Entity.Badge;
import com.example.recompense.Entity.FreelancerRewardProfile;
import com.example.recompense.Repository.FreelancerRewardProfileRepository;
import com.example.recompense.Repository.RecompenseRepository;
import com.example.recompense.Repository.RewardHistoryRepository;
import com.example.recompense.Repository.UserBadgeRepository;
import com.example.recompense.Repository.UserPointsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardEngineServiceTest {

    @Mock private BadgeService badgeService;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private UserPointsRepository userPointsRepository;
    @Mock private FreelancerRewardProfileRepository profileRepository;
    @Mock private RecompenseRepository recompenseRepository;
    @Mock private RewardHistoryRepository rewardHistoryRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @Mock private CertificateService certificateService;

    @InjectMocks
    private RewardEngineService rewardEngineService;

    private RewardEvaluationSyncRequest standardRequest;
    private FreelancerRewardProfile existingProfile;

    @BeforeEach
    void setUp() {
        standardRequest = new RewardEvaluationSyncRequest();
        standardRequest.setFreelancerEmail("freelancer@example.com");
        standardRequest.setFreelancerName("John Doe");
        standardRequest.setCurrentScore(5);
        standardRequest.setAverageScore(4.8); // Meets Elite status
        standardRequest.setTotalPoints(600);
        standardRequest.setTotalEvaluations(10);
        standardRequest.setPositiveEvaluations(9);
        standardRequest.setCompletedProjects(15);

        existingProfile = new FreelancerRewardProfile();
        existingProfile.setUserEmail("freelancer@example.com");
        existingProfile.setUserName("John Doe");
        existingProfile.setCurrentLevel("Niveau 1 - New Freelancer");
    }

    @Test
    @DisplayName("Should promote freelancer to Elite Level when thresholds met")
    void testLevelPromotionToElite() {
        // Arrange
        when(profileRepository.findByUserEmail(anyString())).thenReturn(Optional.of(existingProfile));
        
        // Act
        RewardProcessingResponse response = rewardEngineService.processEvaluation(standardRequest);

        // Assert
        assertEquals("Niveau 4 - Elite Freelancer", existingProfile.getCurrentLevel());
        verify(rewardHistoryRepository, atLeastOnce()).save(any());
        verify(notificationService).createAndBroadcast(eq("freelancer@example.com"), contains("Level updated: Niveau 4"));
    }

    @Test
    @DisplayName("Should award best matching score badge")
    void testAwardScoreBadge() {
        // Arrange
        Badge goldBadge = new Badge();
        goldBadge.setName("Gold Performer");
        goldBadge.setConditionValue(4.5);
        
        when(profileRepository.findByUserEmail(anyString())).thenReturn(Optional.of(existingProfile));
        when(badgeService.findBestAutoBadge(eq("AVERAGE_SCORE"), anyDouble())).thenReturn(goldBadge);
        when(userBadgeRepository.existsByUserNameAndBadgeAndActiveTrue(anyString(), any())).thenReturn(false);

        // Act
        rewardEngineService.processEvaluation(standardRequest);

        // Assert
        assertEquals("Gold Performer", existingProfile.getCurrentScoreBadge());
        verify(userBadgeRepository).save(any());
        verify(emailService).sendBadgeEmail(eq("freelancer@example.com"), contains("New badge unlocked"), anyString(), any(), any());
    }

    @Test
    @DisplayName("Should handle missing profile by creating a new one")
    void testProfileCreation() {
        // Arrange
        when(profileRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());

        // Act
        rewardEngineService.processEvaluation(standardRequest);

        // Assert
        verify(profileRepository, atLeastOnce()).save(any(FreelancerRewardProfile.class));
    }

    @Test
    @DisplayName("Should throw exception if mandatory fields are missing")
    void testValidation() {
        // Arrange
        RewardEvaluationSyncRequest invalidRequest = new RewardEvaluationSyncRequest();
        invalidRequest.setFreelancerEmail(""); // Empty email

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            rewardEngineService.processEvaluation(invalidRequest);
        });
    }

    @Test
    @DisplayName("Should batch assign badge to eligible users")
    void testBatchAssignment() {
        // Arrange
        Badge badge = new Badge();
        badge.setId(101L);
        badge.setName("Fast Learner");
        badge.setAutoAssignable(true);
        badge.setIsActive(true);
        badge.setConditionType("POINTS");
        badge.setConditionValue(100.0);

        when(badgeService.getBadgeById(101L)).thenReturn(Optional.of(badge));
        when(profileRepository.findAll()).thenReturn(java.util.List.of(existingProfile));
        when(userBadgeRepository.existsByUserNameAndBadgeAndActiveTrue(anyString(), any())).thenReturn(false);
        
        // Ensure profile has enough points
        existingProfile.setTotalPoints(150);

        // Act
        int assignedCount = rewardEngineService.assignBadgeToEligibleUsers(101L);

        // Assert
        assertEquals(1, assignedCount);
        verify(userBadgeRepository, times(1)).save(any());
    }
}
