package com.example.recompense.Service;

import com.example.recompense.Entity.Badge;
import com.example.recompense.Repository.BadgeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RewardCatalogSeeder implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    public RewardCatalogSeeder(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    public void run(String... args) {
        if (badgeRepository.count() > 0) {
            return;
        }

        seedBadge("Debutant", "AVERAGE_SCORE", 0.0, "SCORE", false, 10,
                "Assigned when the average score starts the reward journey.");
        seedBadge("Intermediaire", "AVERAGE_SCORE", 2.5, "SCORE", false, 20,
                "Assigned when the average score reaches a reliable level.");
        seedBadge("Professionnel", "AVERAGE_SCORE", 3.5, "SCORE", false, 40,
                "Assigned when the average score reflects strong execution.");
        seedBadge("Expert", "AVERAGE_SCORE", 4.5, "SCORE", true, 60,
                "Assigned to elite freelancers with excellent ratings.");

        seedBadge("Bronze", "POINTS", 100.0, "POINTS", false, 0,
                "Assigned after the first cumulative points milestone.");
        seedBadge("Silver", "POINTS", 250.0, "POINTS", false, 0,
                "Assigned after consistent delivery and point accumulation.");
        seedBadge("Gold", "POINTS", 500.0, "POINTS", true, 0,
                "Assigned to high-performing freelancers with major point totals.");
        seedBadge("Platinum", "POINTS", 1000.0, "POINTS", true, 0,
                "Assigned to top reward performers across the platform.");
    }

    private void seedBadge(String name,
                           String conditionType,
                           double conditionValue,
                           String category,
                           boolean certificateEligible,
                           int pointsReward,
                           String description) {

        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(description);
        badge.setConditionType(conditionType);
        badge.setConditionValue(conditionValue);
        badge.setCategory(category);
        badge.setAutoAssignable(true);
        badge.setCertificateEligible(certificateEligible);
        badge.setPointsReward(pointsReward);
        badge.setIsActive(true);
        badgeRepository.save(badge);
    }
}
