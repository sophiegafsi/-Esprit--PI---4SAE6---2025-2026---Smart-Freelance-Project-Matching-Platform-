package tn.esprit.GestionPortfolio.Services;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ai.chat.client.ChatClient;
import tn.esprit.GestionPortfolio.DTO.SpringAiReviewRequest;
import tn.esprit.GestionPortfolio.DTO.SpringAiReviewResponse;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SpringAiPortfolioAssistantService {

    private static final Pattern METRIC_PATTERN = Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*(%|ms|s|sec|seconds?|minutes?|hours?|days?|users?|clients?|orders?|projects?|tickets?|tasks?|x)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CONTRIBUTION_PATTERN = Pattern.compile("\\b(i|my|we|our|j[' ]?ai|nous|implemented|built|developed|designed|created|optimized|improved|configured|deployed|developpe|concu|realise|cree|mis en place|optimise)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern TECHNOLOGY_PATTERN = Pattern.compile("\\b(java|spring|spring boot|angular|react|vue|sql|mysql|postgres|docker|kubernetes|aws|azure|api|rest|microservice|microservices|oauth|jwt|html|css|typescript|javascript|python|node|redis|rabbitmq|kafka)\\b",
            Pattern.CASE_INSENSITIVE);

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Value("${portfolio.ai.spring.enabled:true}")
    private boolean springAiEnabled;

    @Value("${portfolio.ai.spring.fallback-enabled:true}")
    private boolean fallbackEnabled;

    @Value("${spring.ai.ollama.chat.options.model:qwen2.5:1.5b-instruct}")
    private String configuredModel;

    public SpringAiPortfolioAssistantService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    public SpringAiReviewResponse reviewAchievement(SpringAiReviewRequest request) {
        String title = normalize(request == null ? null : request.title());
        String description = normalize(request == null ? null : request.description());

        if (title.isBlank() && description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title or description must not be blank.");
        }

        if (springAiEnabled) {
            ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
            if (builder != null) {
                try {
                    String feedback = normalize(builder.build()
                            .prompt()
                            .system(buildSystemPrompt())
                            .user(buildUserPrompt(title, description))
                            .call()
                            .content());

                    if (!feedback.isBlank()) {
                        return new SpringAiReviewResponse(
                                title,
                                description,
                                feedback,
                                "Spring AI + Ollama",
                                configuredModel,
                                false,
                                true
                        );
                    }
                } catch (Exception ignored) {
                    // Gracefully fallback when Ollama is not installed, not running, or no model is available.
                }
            }
        }

        if (!fallbackEnabled) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Spring AI local review is unavailable. Start Ollama and pull the configured model.");
        }

        return new SpringAiReviewResponse(
                title,
                description,
                buildFallbackFeedback(title, description),
                "Local fallback review",
                configuredModel,
                true,
                false
        );
    }

    private String buildSystemPrompt() {
        return """
                You are a senior portfolio coach.
                Review the user's achievement title and description.
                Reply in the same language as the user's input.
                Return exactly 3 concise bullet points.
                Each bullet must start with "- ".
                Focus on clarity, measurable impact, technologies, and user/business outcome.
                Do not rewrite the whole text.
                """;
    }

    private String buildUserPrompt(String title, String description) {
        return """
                Title:
                %s

                Description:
                %s
                """.formatted(title.isBlank() ? "(empty)" : title, description.isBlank() ? "(empty)" : description);
    }

    private String buildFallbackFeedback(String title, String description) {
        boolean french = looksFrench(title + " " + description);
        Set<String> suggestions = new LinkedHashSet<>();

        if (title.length() < 12 || isGenericTitle(title)) {
            suggestions.add(french
                    ? "- Rends le titre plus precis en indiquant le module, le livrable ou le contexte metier."
                    : "- Make the title more specific by naming the module, deliverable, or business context.");
        }

        if (!containsMetric(description)) {
            suggestions.add(french
                    ? "- Ajoute au moins un resultat mesurable comme un gain de temps, un pourcentage, ou un volume traite."
                    : "- Add at least one measurable result such as time saved, a percentage, or processed volume.");
        }

        if (!containsTechnologySignal(description)) {
            suggestions.add(french
                    ? "- Cite les technologies, outils ou frameworks utilises pour rendre la competence plus credible."
                    : "- Mention the technologies, tools, or frameworks used to make the achievement more credible.");
        }

        if (!containsContributionSignal(description)) {
            suggestions.add(french
                    ? "- Precise ton role exact avec un verbe d'action clair comme developpe, concu, integre ou optimise."
                    : "- Clarify your exact contribution with a strong action verb such as developed, designed, integrated, or optimized.");
        }

        if (description.length() < 90) {
            suggestions.add(french
                    ? "- Allonge la description en 2 ou 3 phrases pour couvrir le besoin, ton action, puis l'impact final."
                    : "- Expand the description to 2 or 3 sentences covering the need, your action, and the final impact.");
        }

        suggestions.add(french
                ? "- Termine par la valeur apportee a l'utilisateur final, au client, ou a l'equipe."
                : "- End with the value delivered to the end user, client, or team.");

        return suggestions.stream()
                .limit(3)
                .reduce((left, right) -> left + "\n" + right)
                .orElse(french
                        ? "- Decris le besoin, ton action, puis le resultat obtenu."
                        : "- Describe the need, your action, and the achieved result.");
    }

    private boolean looksFrench(String value) {
        String normalized = normalize(value).toLowerCase(Locale.ROOT);
        return normalized.matches(".*\\b(le|la|les|des|une|avec|pour|dans|sur|nous|j ai|realise|application|plateforme|gestion)\\b.*");
    }

    private boolean isGenericTitle(String title) {
        String normalized = normalize(title).toLowerCase(Locale.ROOT);
        return normalized.matches("^(project|projet|application|app|website|site web|portfolio|achievement|work|task)$");
    }

    private boolean containsMetric(String description) {
        return METRIC_PATTERN.matcher(normalize(description)).find();
    }

    private boolean containsContributionSignal(String description) {
        return CONTRIBUTION_PATTERN.matcher(normalize(description)).find();
    }

    private boolean containsTechnologySignal(String description) {
        return TECHNOLOGY_PATTERN.matcher(normalize(description)).find();
    }

    private String normalize(String value) {
        return String.valueOf(value == null ? "" : value).trim().replaceAll("\\s+", " ");
    }
}
