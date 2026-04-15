package com.example.evaluation_service.Controller;

import com.example.evaluation_service.Entity.Review;
import com.example.evaluation_service.Service.GeminiRestService;
import com.example.evaluation_service.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    private GeminiRestService sentimentService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ✅ Ancien endpoint - inchangé
    @PostMapping("/add")
    public Review addReview(@RequestBody Review review) {
        return reviewService.addReview(review);
    }

    // ✅ Ancien endpoint - inchangé (pour admin)
    @GetMapping("/all")
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // ✅ NOUVEAU ENDPOINT: Récupérer les reviews par email utilisateur
    @GetMapping("/user/{email}")
    public List<Review> getReviewsByUserEmail(@PathVariable String email) {
        return reviewService.getReviewsByUserEmail(email);
    }

    // ✅ NOUVEAU ENDPOINT: Récupérer les reviews avec filtre optionnel par email
    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) String userEmail) {
        if (userEmail != null && !userEmail.isEmpty()) {
            return reviewService.getReviewsByUserEmail(userEmail);
        }
        return reviewService.getAllReviews();
    }

    // ✅ Ancien endpoint - inchangé
    @GetMapping("/evaluation/{evaluationId}")
    public List<Review> getReviewsByEvaluation(@PathVariable Long evaluationId) {
        return reviewService.getReviewsByEvaluationId(evaluationId);
    }

    // ✅ Ancien endpoint - inchangé
    @DeleteMapping("/delete/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    // ✅ Ancien endpoint - inchangé
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    // ✅ Ancien endpoint - inchangé
    @PutMapping("/update/{id}")
    public Review updateReview(@PathVariable Long id, @RequestBody Review review) {
        return reviewService.updateReview(id, review);
    }


    @GetMapping("/sentiment-stats")
    public Map<String, Long> getSentimentStats() {
        return reviewService.getSentimentStats();
    }


    @PostMapping("/sentiment/analyze")
    public Map<String, Object> analyzeSentiment(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sentiment = sentimentService.analyzeSentiment(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("sentiment", sentiment);
        return response;
    }


    @GetMapping("/sentiment/analyze")
    public Map<String, Object> analyzeSentimentGet(@RequestParam String text) {
        String sentiment = sentimentService.analyzeSentiment(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("sentiment", sentiment);
        return response;
    }


    @GetMapping("/sentiment/cache/size")
    public int getCacheSize() {
        return sentimentService.getCacheSize();
    }

    // ✅ Ancien endpoint - inchangé
    @DeleteMapping("/sentiment/cache")
    public String clearCache() {
        sentimentService.clearCache();
        return "Cache vidé avec succès";
    }
}