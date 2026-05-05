package com.example.reclamation.Controllers;

import com.example.reclamation.dto.SentimentResultDTO;
import com.example.reclamation.services.SentimentAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sentiment")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SentimentAnalysisController {

    private final SentimentAnalysisService sentimentAnalysisService;

    @PostMapping("/analyze")
    public SentimentResultDTO analyze(@RequestBody Map<String, String> body) {
        String message = body.get("message");

        SentimentResultDTO result = new SentimentResultDTO();
        result.setSentiment(sentimentAnalysisService.detectSentiment(message));
        result.setReason(sentimentAnalysisService.getSentimentReason(message));

        return result;
    }
}