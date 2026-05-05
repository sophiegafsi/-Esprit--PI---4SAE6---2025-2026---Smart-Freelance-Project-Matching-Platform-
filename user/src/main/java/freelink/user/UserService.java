package freelink.user;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {
    User syncUser(OidcUser oidcUser);

    User syncUser(Jwt jwt);

    List<User> findAll();

    User findByEmail(String email);

    java.util.Optional<User> findByKeycloakId(String keycloakId);

    User findById(UUID id);

    User update(UUID id, User user);

    void delete(UUID id);

    void registerUser(freelink.user.dto.UserDTO userDTO);

    void forgotPassword(String email);

    User becomeFreelancer(User user, freelink.user.dto.FreelancerProfileDTO profileDTO);

    // Notifications
    List<Notification> getUserNotifications(UUID userId);
    Notification createNotification(UUID userId, String message, String type, String actionUrl);
    void markNotificationAsRead(UUID notificationId);
}
