package com.example.evaluation_service.Service;

import com.example.evaluation_service.DTO.RewardEvaluationSyncRequest;
import com.example.evaluation_service.Entity.Evaluation;
import com.example.evaluation_service.Entity.Review;
import com.example.evaluation_service.Repository.EvaluationRepository;
import com.example.evaluation_service.client.RecompenseClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final RecompenseClient recompenseClient;

    public EvaluationService(EvaluationRepository evaluationRepository,
                             RecompenseClient recompenseClient) {
        this.evaluationRepository = evaluationRepository;
        this.recompenseClient = recompenseClient;
    }

    public Evaluation createEvaluation(Evaluation evaluation) {
        if (evaluation.getAvis() != null && !evaluation.getAvis().isEmpty()) {
            double score = calculateScoreFromReviews(evaluation.getAvis());
            evaluation.setScore((int) Math.round(score));
        }

        Evaluation saved = evaluationRepository.save(evaluation);
        syncRewards(saved.getEvaluatedUserEmail(), saved.getEvaluatedUserName(), saved);
        return saved;
    }

    public List<Evaluation> getAllEvaluations() {
        return evaluationRepository.findAll();
    }

    public List<Evaluation> getEvaluationsByUserEmail(String email) {
        return evaluationRepository.findByUserEmail(email);
    }

    public Optional<Evaluation> getEvaluationById(Long id) {
        return evaluationRepository.findById(id);
    }

    public Evaluation updateEvaluation(Long id, Evaluation updatedEvaluation) {
        return evaluationRepository.findById(id).map(evaluation -> {
            evaluation.setComment(updatedEvaluation.getComment());
            evaluation.setAnonymous(updatedEvaluation.isAnonymous());
            evaluation.setProjectName(updatedEvaluation.getProjectName());
            evaluation.setEvaluatorName(updatedEvaluation.getEvaluatorName());
            evaluation.setEvaluatedUserName(updatedEvaluation.getEvaluatedUserName());
            evaluation.setEvaluatedUserEmail(updatedEvaluation.getEvaluatedUserEmail());
            evaluation.setTypeEvaluation(updatedEvaluation.getTypeEvaluation());

            if (updatedEvaluation.getAvis() != null) {
                evaluation.setAvis(updatedEvaluation.getAvis());
                double score = calculateScoreFromReviews(updatedEvaluation.getAvis());
                evaluation.setScore((int) Math.round(score));
            }

            Evaluation saved = evaluationRepository.save(evaluation);
            syncRewards(saved.getEvaluatedUserEmail(), saved.getEvaluatedUserName(), saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Evaluation not found"));
    }

    public void deleteEvaluation(Long id) {
        Evaluation existing = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        String freelancerEmail = existing.getEvaluatedUserEmail();
        String freelancerName = existing.getEvaluatedUserName();

        evaluationRepository.deleteById(id);
        syncRewards(freelancerEmail, freelancerName, null);
    }

    public double getAverageScoreForUser(String userName) {
        List<Evaluation> evaluations = evaluationRepository.findByEvaluatedUserName(userName);
        if (evaluations.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Evaluation evaluation : evaluations) {
            if (evaluation.getScore() != null) {
                sum += evaluation.getScore();
            }
        }

        return Math.round((sum / evaluations.size()) * 10.0) / 10.0;
    }

    public double calculateScoreFromReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Review review : reviews) {
            if (review.getScore() != null) {
                sum += review.getScore();
            }
        }

        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }

    private void syncRewards(String freelancerEmail, String freelancerName, Evaluation preferredEvaluation) {
        if (freelancerEmail == null || freelancerEmail.isBlank()) {
            return;
        }

        List<Evaluation> evaluations = evaluationRepository.findByEvaluatedUserEmail(freelancerEmail);
        RewardEvaluationSyncRequest request = buildRewardRequest(
                freelancerEmail,
                freelancerName,
                preferredEvaluation,
                evaluations
        );

        try {
            recompenseClient.processEvaluation(request);
        } catch (Exception ex) {
            System.err.println("Unable to sync reward engine for " + freelancerEmail + ": " + ex.getMessage());
        }
    }

    private RewardEvaluationSyncRequest buildRewardRequest(String freelancerEmail,
                                                           String freelancerName,
                                                           Evaluation preferredEvaluation,
                                                           List<Evaluation> evaluations) {

        Evaluation latest = resolveLatestEvaluation(preferredEvaluation, evaluations);

        RewardEvaluationSyncRequest request = new RewardEvaluationSyncRequest();
        request.setFreelancerEmail(freelancerEmail);
        request.setFreelancerName(resolveFreelancerName(freelancerName, latest));
        request.setEvaluationId(latest != null ? latest.getId() : null);
        request.setProjectName(latest != null ? latest.getProjectName() : null);
        request.setCurrentScore(latest != null && latest.getScore() != null ? latest.getScore() : 0);
        request.setAverageScore(calculateAverageScore(evaluations));
        request.setTotalEvaluations(evaluations.size());
        request.setPositiveEvaluations((int) evaluations.stream()
                .filter(evaluation -> evaluation.getScore() != null && evaluation.getScore() >= 4)
                .count());
        request.setCompletedProjects((int) evaluations.stream()
                .map(Evaluation::getProjectName)
                .filter(projectName -> projectName != null && !projectName.isBlank())
                .distinct()
                .count());
        request.setTotalPoints(calculateTotalPoints(evaluations));
        request.setEvaluatedAt(LocalDateTime.now());
        return request;
    }

    private Evaluation resolveLatestEvaluation(Evaluation preferredEvaluation, List<Evaluation> evaluations) {
        if (preferredEvaluation != null) {
            return preferredEvaluation;
        }

        return evaluations.stream()
                .max(Comparator.comparing(this::evaluationMoment))
                .orElse(null);
    }

    private LocalDateTime evaluationMoment(Evaluation evaluation) {
        if (evaluation.getUpdatedAt() != null) {
            return LocalDateTime.ofInstant(evaluation.getUpdatedAt().toInstant(), ZoneId.systemDefault());
        }
        if (evaluation.getDate() != null) {
            return LocalDateTime.ofInstant(evaluation.getDate().toInstant(), ZoneId.systemDefault());
        }
        return LocalDateTime.MIN;
    }

    private String resolveFreelancerName(String fallbackName, Evaluation latestEvaluation) {
        if (latestEvaluation != null
                && latestEvaluation.getEvaluatedUserName() != null
                && !latestEvaluation.getEvaluatedUserName().isBlank()) {
            return latestEvaluation.getEvaluatedUserName();
        }
        return fallbackName;
    }

    private double calculateAverageScore(List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int count = 0;
        for (Evaluation evaluation : evaluations) {
            if (evaluation.getScore() != null) {
                sum += evaluation.getScore();
                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return Math.round((sum / count) * 100.0) / 100.0;
    }

    private int calculateTotalPoints(List<Evaluation> evaluations) {
        return evaluations.stream()
                .filter(evaluation -> evaluation.getScore() != null)
                .mapToInt(evaluation -> Math.max(evaluation.getScore(), 0) * 10)
                .sum();
    }
}
