package com.example.recompense.Controller;

import com.example.recompense.Entity.Notification;
import com.example.recompense.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/create")
    @PreAuthorize("hasRole('admin')")
    public Notification create(@RequestParam String email,
                               @RequestParam String message) {
        return service.createAndBroadcast(email, message);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessEmail(authentication, #email)")
    public List<Notification> getUserNotifications(@PathVariable String email) {
        return service.getUserNotifications(email);
    }
}
