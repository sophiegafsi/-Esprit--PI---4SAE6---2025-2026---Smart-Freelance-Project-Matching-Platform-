package tn.esprit.GestionPortfolio.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.GestionPortfolio.DTO.SpringAiReviewRequest;
import tn.esprit.GestionPortfolio.DTO.SpringAiReviewResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAiPortfolioAssistantServiceTest {

    private SpringAiPortfolioAssistantService springAiPortfolioAssistantService;

    @BeforeEach
    void setUp() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        springAiPortfolioAssistantService =
                new SpringAiPortfolioAssistantService(beanFactory.getBeanProvider(org.springframework.ai.chat.client.ChatClient.Builder.class));

        ReflectionTestUtils.setField(springAiPortfolioAssistantService, "springAiEnabled", false);
        ReflectionTestUtils.setField(springAiPortfolioAssistantService, "fallbackEnabled", true);
        ReflectionTestUtils.setField(springAiPortfolioAssistantService, "configuredModel", "qwen2.5:1.5b-instruct");
    }

    @Test
    void shouldReturnFallbackReviewWhenSpringAiIsDisabled() {
        SpringAiReviewResponse response = springAiPortfolioAssistantService.reviewAchievement(
                new SpringAiReviewRequest("Application", "Built a platform for users.")
        );

        assertThat(response.fallbackUsed()).isTrue();
        assertThat(response.available()).isFalse();
        assertThat(response.provider()).isEqualTo("Local fallback review");
        assertThat(response.feedback()).contains("- ");
    }

    @Test
    void shouldRejectBlankTitleAndDescription() {
        assertThatThrownBy(() -> springAiPortfolioAssistantService.reviewAchievement(
                new SpringAiReviewRequest("   ", "   ")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Title or description must not be blank");
    }
}
