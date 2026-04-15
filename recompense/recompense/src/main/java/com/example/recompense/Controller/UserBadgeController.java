package com.example.recompense.Controller;

import com.example.recompense.DTO.UserBadgeDTO;
import com.example.recompense.Service.UserBadgeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-badges")
public class UserBadgeController {

    private final UserBadgeService userBadgeService;

    public UserBadgeController(UserBadgeService userBadgeService) {
        this.userBadgeService = userBadgeService;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('admin')")
    public void assignBadge(@RequestParam String userName,
                            @RequestParam String badgeName) {
        userBadgeService.assignBadge(userName, badgeName);
    }

    @GetMapping("/badges/{userName}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessEmail(authentication, #userName)")
    public List<UserBadgeDTO> getBadges(@PathVariable String userName) {
        return userBadgeService.getUserBadges(userName);
    }

    @GetMapping("/active/{userName}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessEmail(authentication, #userName)")
    public List<UserBadgeDTO> getActiveBadges(@PathVariable String userName) {
        return userBadgeService.getActiveBadges(userName);
    }
}
