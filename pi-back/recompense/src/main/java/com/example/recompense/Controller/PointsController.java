package com.example.recompense.Controller;

import com.example.recompense.Repository.UserPointsRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "http://localhost:4200")
public class PointsController {

    private final UserPointsRepository userPointsRepository;

    public PointsController(UserPointsRepository userPointsRepository) {
        this.userPointsRepository = userPointsRepository;
    }

    @GetMapping("/points/{email}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessEmail(authentication, #email)")
    public int getPoints(@PathVariable String email) {
        return userPointsRepository.getTotalPoints(email);
    }

    @GetMapping("/points/progress/{email}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessEmail(authentication, #email)")
    public int getProgress(@PathVariable String email) {
        int points = userPointsRepository.getTotalPoints(email);
        int[] milestones = {100, 250, 500, 1000};
        int previous = 0;

        for (int milestone : milestones) {
            if (points < milestone) {
                return (int) Math.round(((double) (points - previous) / (milestone - previous)) * 100);
            }
            previous = milestone;
        }

        return 100;
    }
}
