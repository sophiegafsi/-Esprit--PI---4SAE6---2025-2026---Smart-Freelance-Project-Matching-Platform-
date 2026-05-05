package com.example.recompense.Service;

import com.example.recompense.DTO.UserBadgeDTO;
import com.example.recompense.Entity.Badge;
import com.example.recompense.Entity.UserBadge;
import com.example.recompense.Repository.BadgeRepository;
import com.example.recompense.Repository.UserBadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final NotificationService notificationService;

    public UserBadgeService(UserBadgeRepository userBadgeRepository,
                            BadgeRepository badgeRepository,
                            NotificationService notificationService) {
        this.userBadgeRepository = userBadgeRepository;
        this.badgeRepository = badgeRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void assignBadge(String userName, String badgeName) {
        Badge badge = badgeRepository.findByNameIgnoreCase(badgeName)
                .orElseThrow(() -> new RuntimeException("Badge not found: " + badgeName));

        boolean exists = userBadgeRepository.existsByUserNameAndBadgeAndActiveTrue(userName, badge);
        if (exists) {
            return;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserName(userName);
        userBadge.setDisplayName(userName);
        userBadge.setBadge(badge);
        userBadge.setDateAssigned(LocalDateTime.now());
        userBadge.setActive(true);
        userBadge.setStatusReason("Badge assigned manually by admin.");

        userBadgeRepository.save(userBadge);
        notificationService.createAndBroadcast(userName, "Badge assigned manually: " + badge.getName());
    }

    public List<UserBadgeDTO> getUserBadges(String userName) {
        return toDtoList(userBadgeRepository.findByUserNameWithBadge(userName));
    }

    public List<UserBadgeDTO> getActiveBadges(String userName) {
        return toDtoList(userBadgeRepository.findByUserNameAndActiveTrueOrderByDateAssignedDesc(userName));
    }

    private List<UserBadgeDTO> toDtoList(List<UserBadge> badges) {
        return badges.stream()
                .map(this::toDto)
                .toList();
    }

    private UserBadgeDTO toDto(UserBadge userBadge) {
        UserBadgeDTO dto = new UserBadgeDTO();
        dto.setBadgeName(userBadge.getBadge().getName());
        dto.setDescription(userBadge.getBadge().getDescription());
        dto.setIcon(userBadge.getBadge().getIcon());
        dto.setDateAssigned(userBadge.getDateAssigned());
        dto.setActive(userBadge.isActive());
        dto.setStatusReason(userBadge.getStatusReason());
        dto.setCertificateGenerated(userBadge.isCertificateGenerated());
        dto.setConditionType(userBadge.getBadge().getConditionType());
        dto.setConditionValue(userBadge.getBadge().getConditionValue());
        return dto;
    }
}
