package tn.esprit.GestionPortfolio.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Repository.AchievementRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AchievementServiceImplTest {

    private AchievementRepository achievementRepository;
    private AchievementServiceImpl achievementService;

    @BeforeEach
    void setUp() {
        achievementRepository = mock(AchievementRepository.class);
        ProfanityFilterService profanityFilterService =
                new ProfanityFilterService("classpath:bad-words-dictionary.txt", "", new DefaultResourceLoader());

        achievementService = new AchievementServiceImpl(achievementRepository, profanityFilterService);
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldMaskTitleAndDescriptionBeforeSaving() {
        Achievement achievement = Achievement.builder()
                .title("Connard API")
                .description("This is f.u.c.k in production.")
                .completionDate(LocalDate.of(2026, 4, 15))
                .freelancerId(1L)
                .build();

        Achievement saved = achievementService.addAchievement(achievement);
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);

        verify(achievementRepository).save(captor.capture());

        Achievement persisted = captor.getValue();
        assertThat(persisted.getTitle()).isEqualTo("******* API");
        assertThat(persisted.getDescription()).isEqualTo("This is ******* in production.");
        assertThat(saved.getDescription()).isEqualTo("This is ******* in production.");
    }
}
