package com.example.recompense.Security;

import com.example.recompense.Entity.RewardHistory;
import com.example.recompense.Repository.RewardHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component("rewardSecurity")
public class RewardSecurity {

    private final RewardHistoryRepository rewardHistoryRepository;

    public RewardSecurity(RewardHistoryRepository rewardHistoryRepository) {
        this.rewardHistoryRepository = rewardHistoryRepository;
    }

    public boolean canAccessEmail(Authentication authentication, String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (hasRole(authentication, "ROLE_admin")) {
            return true;
        }

        String currentEmail = extractEmail(authentication);
        return currentEmail != null && currentEmail.equalsIgnoreCase(email.trim());
    }

    public boolean canAccessHistory(Authentication authentication, Long historyId) {
        if (historyId == null) {
            return false;
        }
        if (hasRole(authentication, "ROLE_admin")) {
            return true;
        }

        Optional<RewardHistory> history = rewardHistoryRepository.findById(historyId);
        return history.map(RewardHistory::getUserEmail)
                .filter(Objects::nonNull)
                .filter(email -> canAccessEmail(authentication, email))
                .isPresent();
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private String extractEmail(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken().getClaimAsString("email");
        }

        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }

        return authentication == null ? null : authentication.getName();
    }
}
