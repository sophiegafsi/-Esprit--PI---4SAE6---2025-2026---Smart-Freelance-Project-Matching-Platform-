package tn.esprit.messagerie.services;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class ContentValidatorService {


    private static final String PHONE_REGEX = "(?:\\+|00)?\\d{1,4}[\\s-]?\\d{2,3}[\\s-]?\\d{3}[\\s-]?\\d{3}|\\d{8,15}";


    private static final String EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

    private static final String URL_REGEX = "(https?://|www\\.)\\S+|\\b[a-zA-Z0-9.-]+\\.(com|fr|org|net|me|io|tk|ml|ga|cf|gq)\\b";


    private static final String[] FORBIDDEN_PHRASES = {
            "pay me outside",
            "contact me on whatsapp",
            "let's move to telegram",
            "send payment directly",
            "avoid platform fees",
            "t.me/",
            "wa.me/",
            "fuck",
            "mother fucker",
            "pay me in ",
            "messanger",
            "instagram",
            "snapchat",
            "rip",

    };

    private final Pattern phonePattern = Pattern.compile(PHONE_REGEX);
    private final Pattern emailPattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
    private final Pattern urlPattern = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);

    public boolean containsForbiddenContent(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }


        if (phonePattern.matcher(content).find()) {
            return true;
        }


        if (emailPattern.matcher(content).find()) {
            return true;
        }


        if (urlPattern.matcher(content).find()) {
            return true;
        }


        String lowerContent = content.toLowerCase();
        return Stream.of(FORBIDDEN_PHRASES).anyMatch(lowerContent::contains);
    }

    public String maskForbiddenContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String masked = content;


        masked = phonePattern.matcher(masked).replaceAll("*****");


        masked = emailPattern.matcher(masked).replaceAll("*****");


        masked = urlPattern.matcher(masked).replaceAll("*****");


        for (String phrase : FORBIDDEN_PHRASES) {
            masked = masked.replaceAll("(?i)" + Pattern.quote(phrase), "*****");
        }

        return masked;
    }
}
