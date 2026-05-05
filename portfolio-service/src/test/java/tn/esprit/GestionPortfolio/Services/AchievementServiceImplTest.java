package tn.esprit.GestionPortfolio.Services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AchievementServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @Test
    void testAddAchievement_Success() {
        Achievement achievement = new Achievement();
        achievement.setTitle("Expert Developer");
        achievement.setDescription("Developed complex microservices.");

        when(profanityFilterService.mask(anyString())).thenReturn("Expert Developer");
        when(achievementRepository.save(any(Achievement.class))).thenReturn(achievement);

        // We need to mock the SecurityContext for getCurrentUserId()
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("test-user");
        var context = mock(org.springframework.security.core.context.SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        Achievement result = achievementService.addAchievement(achievement);

        assertNotNull(result);
        verify(achievementRepository).save(any(Achievement.class));
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}
