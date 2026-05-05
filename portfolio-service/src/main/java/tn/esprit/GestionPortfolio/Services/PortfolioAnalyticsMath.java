package tn.esprit.GestionPortfolio.Services;

import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.ContributionLevel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Centralise les petites formules communes pour garder les services lisibles.
public final class PortfolioAnalyticsMath {

    public static final double NEUTRAL_QUALITY_SCORE = 5.0;
    public static final double MAX_SCORE = 100.0;
    public static final double FULL_COMPONENT_SCORE = 25.0;
    public static final int MAX_ACHIEVEMENTS_FOR_FULL_SCORE = 5;
    public static final int MAX_SKILLS_FOR_FULL_SCORE = 8;
    public static final int MAX_OCCURRENCES_FOR_BONUS = 5;

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private PortfolioAnalyticsMath() {
    }

    // Si un projet n'a pas encore de métrique, on lui donne une valeur neutre.
    public static double metricComplexity(AchievementMetric metric) {
        return metric == null ? NEUTRAL_QUALITY_SCORE : clampScore(metric.getComplexityScore());
    }

    public static double metricImpact(AchievementMetric metric) {
        return metric == null ? NEUTRAL_QUALITY_SCORE : clampScore(metric.getImpactScore());
    }

    public static double projectQuality(AchievementMetric metric) {
        return (metricComplexity(metric) + metricImpact(metric)) / 2.0;
    }

    public static double metricDuration(AchievementMetric metric) {
        if (metric == null || metric.getDurationDays() == null) {
            return 0.0;
        }
        return Math.max(metric.getDurationDays(), 0);
    }

    // Convertit LOW / MEDIUM / HIGH en poids simple pour les calculs.
    public static double contributionWeight(ContributionLevel level) {
        if (level == null) {
            return 1.0;
        }
        return switch (level) {
            case LOW -> 1.0;
            case MEDIUM -> 2.0;
            case HIGH -> 3.0;
        };
    }

    // Sert à transformer une quantité brute en composant de score borné.
    public static double componentScore(double value, double maxValue, double maxScore) {
        if (maxValue <= 0) {
            return 0.0;
        }
        return Math.min(value, maxValue) / maxValue * maxScore;
    }

    public static double average(double total, int count) {
        if (count <= 0) {
            return 0.0;
        }
        return total / count;
    }

    public static String profileLevel(double score) {
        if (score >= 75.0) {
            return "ELITE";
        }
        if (score >= 45.0) {
            return "STRONG";
        }
        return "DEVELOPING";
    }

    public static String formatPeriod(LocalDate completionDate) {
        if (completionDate == null) {
            return "unknown";
        }
        return PERIOD_FORMATTER.format(completionDate);
    }

    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double clampScore(Integer score) {
        if (score == null) {
            return NEUTRAL_QUALITY_SCORE;
        }
        return Math.max(0.0, Math.min(10.0, score));
    }
}
