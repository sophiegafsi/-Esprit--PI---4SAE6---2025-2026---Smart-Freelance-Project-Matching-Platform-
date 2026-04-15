package com.example.evaluation_service.Service;

import com.example.evaluation_service.Entity.Review;
import com.example.evaluation_service.Repository.ReviewRepository;
import com.example.evaluation_service.Repository.EvaluationRepository;
import com.example.evaluation_service.Entity.Evaluation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    private GeminiRestService sentimentService;

    @Autowired
    private EvaluationRepository evaluationRepository;

    public ReviewService(ReviewRepository reviewRepository, GeminiRestService sentimentService) {
        this.reviewRepository = reviewRepository;
        this.sentimentService = sentimentService;
    }

    public Review addReview(Review review) {

        // Analyse sentiment
        String sentiment = sentimentService.analyzeSentiment(review.getComment());
        review.setSentiment(sentiment);

        // Save review
        Review savedReview = reviewRepository.save(review);

        // 🔁 Recalcul score evaluation
        if (savedReview.getEvaluation() != null) {

            Long evaluationId = savedReview.getEvaluation().getId();

            List<Review> reviews = reviewRepository.findByEvaluation_Id(evaluationId);

            double avg = reviews.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToInt(Review::getScore)
                    .average()
                    .orElse(0);

            Evaluation evaluation = evaluationRepository.findById(evaluationId)
                    .orElseThrow(() -> new RuntimeException("Evaluation non trouvée"));

            evaluation.setScore((int) Math.round(avg));

            evaluationRepository.save(evaluation);
        }

        return savedReview;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> getReviewsByEvaluationId(Long evaluationId) {
        return reviewRepository.findByEvaluation_Id(evaluationId);
    }

    public List<Review> getReviewsByUserEmail(String userEmail) {
        return reviewRepository.findByUserEmail(userEmail);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public Review updateReview(Long id, Review reviewDetails) {

        Review review = getReviewById(id);

        review.setScore(reviewDetails.getScore());
        review.setComment(reviewDetails.getComment());
        review.setEvaluatorName(reviewDetails.getEvaluatorName());

        String sentiment = sentimentService.analyzeSentiment(reviewDetails.getComment());
        review.setSentiment(sentiment);

        Review updated = reviewRepository.save(review);

        // 🔁 Recalcul score evaluation
        if (updated.getEvaluation() != null) {

            Long evaluationId = updated.getEvaluation().getId();

            List<Review> reviews = reviewRepository.findByEvaluation_Id(evaluationId);

            double avg = reviews.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToInt(Review::getScore)
                    .average()
                    .orElse(0);

            Evaluation evaluation = evaluationRepository.findById(evaluationId)
                    .orElseThrow(() -> new RuntimeException("Evaluation non trouvée"));

            evaluation.setScore((int) Math.round(avg));

            evaluationRepository.save(evaluation);
        }

        return updated;
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review non trouvée"));
    }

    public Map<String, Long> getSentimentStats() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .filter(r -> r.getSentiment() != null)
                .collect(Collectors.groupingBy(
                        Review::getSentiment,
                        Collectors.counting()
                ));
    }

    public long countReviewsByUserEmail(String userEmail) {
        return reviewRepository.countByUserEmail(userEmail);
    }

    public boolean hasReviews(String userEmail) {
        return reviewRepository.existsByUserEmail(userEmail);
    }

    public void deleteAllReviewsByUserEmail(String userEmail) {
        List<Review> reviews = reviewRepository.findByUserEmail(userEmail);
        reviewRepository.deleteAll(reviews);
    }
}