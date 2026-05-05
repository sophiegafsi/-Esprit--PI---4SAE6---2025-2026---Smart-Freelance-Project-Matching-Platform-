package com.example.recompense.Controller;

import com.example.recompense.Entity.Badge;
import com.example.recompense.Service.BadgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")

public class BadgeController {

    private final BadgeService badgeService;
    private final com.example.recompense.Service.RewardEngineService rewardEngineService;

    public BadgeController(BadgeService badgeService, com.example.recompense.Service.RewardEngineService rewardEngineService) {
        this.badgeService = badgeService;
        this.rewardEngineService = rewardEngineService;
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<java.util.Map<String, Object>> assignBadge(@PathVariable Long id) {
        int assignedCount = rewardEngineService.assignBadgeToEligibleUsers(id);
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Badge assignment process completed.",
                "assignedRewards", assignedCount
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Badge> getAllBadges() {
        return badgeService.getAllBadges();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Badge> getBadgeById(@PathVariable Long id) {
        return badgeService.getBadgeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('admin','client','freelancer')")
    public List<Badge> getActiveBadges() {
        return badgeService.getActiveBadges();
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Badge> createBadge(@RequestBody Badge badge) {
        Badge savedBadge = badgeService.createBadge(badge);
        return new ResponseEntity<>(savedBadge, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Badge> updateBadge(@PathVariable Long id, @RequestBody Badge badgeDetails) {
        return ResponseEntity.ok(badgeService.updateBadge(id, badgeDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }
}
