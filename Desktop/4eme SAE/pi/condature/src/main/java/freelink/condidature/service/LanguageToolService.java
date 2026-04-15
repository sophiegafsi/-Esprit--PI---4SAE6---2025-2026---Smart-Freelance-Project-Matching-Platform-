package freelink.condidature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LanguageToolService {

    private final RestTemplate restTemplate;
    private static final String LT_API_URL = "https://api.languagetool.org/v2/check";

    public String autoCorrectCoverLetter(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("text", text);
            map.add("language", "en-US");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            Map<String, Object> response = restTemplate.postForObject(LT_API_URL, request, Map.class);

            if (response != null && response.containsKey("matches")) {
                List<Map<String, Object>> matches = (List<Map<String, Object>>) response.get("matches");

                if (matches.isEmpty()) {
                    return text;
                }

                // Sort matches in reverse order so applying them doesn't mess up subsequent
                // offsets
                matches.sort((m1, m2) -> {
                    Integer o1 = (Integer) m1.get("offset");
                    Integer o2 = (Integer) m2.get("offset");
                    return o2.compareTo(o1);
                });

                StringBuilder correctedText = new StringBuilder(text);
                for (Map<String, Object> match : matches) {
                    List<Map<String, String>> replacements = (List<Map<String, String>>) match.get("replacements");
                    if (replacements != null && !replacements.isEmpty()) {
                        String replacement = replacements.get(0).get("value");
                        if (replacement != null && !replacement.isEmpty()) {
                            int offset = (Integer) match.get("offset");
                            int length = (Integer) match.get("length");
                            correctedText.replace(offset, offset + length, replacement);
                        }
                    }
                }
                return correctedText.toString();
            }
        } catch (Exception e) {
            System.err.println("LanguageTool API Error: " + e.getMessage());
            return text;
        }

        return text;
    }
}
