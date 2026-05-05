package com.example.reclamation.Controllers;

import com.example.reclamation.dto.ModerationResultDTO;
import com.example.reclamation.services.ReponseModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reponses")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ReponseModerationController {

    private final ReponseModerationService moderationService;

    @PostMapping("/moderate")
    public ModerationResultDTO moderate(@RequestBody Map<String, String> body) {
        String message = body.get("message");

        ModerationResultDTO result = new ModerationResultDTO();

        if (message == null || message.trim().isEmpty()) {
            result.setAllowed(false);
            result.setReason("Le message est vide.");
            result.setSuggestion("");
            return result;
        }

        if (moderationService.containsBadWords(message)) {
            String badWord = moderationService.detectBadWord(message);

            result.setAllowed(false);
            result.setReason("Message inapproprié détecté : " + badWord);
            result.setSuggestion(moderationService.suggestProfessionalMessage(message));
        } else {
            result.setAllowed(true);
            result.setReason("");
            result.setSuggestion("");
        }

        return result;
    }
}