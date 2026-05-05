package tn.esprit.GestionPortfolio.Services;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class ProfanityFilterServiceTest {

    private final ProfanityFilterService profanityFilterService =
            new ProfanityFilterService("classpath:bad-words-dictionary.txt", "", new DefaultResourceLoader());

    @Test
    void shouldMaskKnownWordsIgnoringCase() {
        String masked = profanityFilterService.mask("Merde and IDIOT should not stay visible.");

        assertThat(masked).isEqualTo("***** and ***** should not stay visible.");
    }

    @Test
    void shouldMaskObfuscatedWordsWithSymbolsAndDigits() {
        String masked = profanityFilterService.mask("This is f.u.c.k and c0nnard.");

        assertThat(masked).doesNotContain("f.u.c.k");
        assertThat(masked).doesNotContain("c0nnard");
        assertThat(masked).isEqualTo("This is ******* and *******.");
    }

    @Test
    void shouldLeaveCleanTextUntouched() {
        String cleanText = "Professional portfolio delivery for a mobile application.";

        assertThat(profanityFilterService.mask(cleanText)).isEqualTo(cleanText);
    }

    @Test
    void shouldLoadCustomWordsFromConfiguration() {
        ProfanityFilterService customFilter =
                new ProfanityFilterService("classpath:bad-words-dictionary.txt", "darkpattern", new DefaultResourceLoader());

        assertThat(customFilter.mask("This flow hides a darkpattern from users."))
                .isEqualTo("This flow hides a *********** from users.");
    }
}
