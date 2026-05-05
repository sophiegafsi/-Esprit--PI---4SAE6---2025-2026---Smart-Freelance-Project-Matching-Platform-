package freelink.user;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import jakarta.persistence.EntityManager;
import java.util.Optional;

import freelink.user.service.KeycloakService;
import java.util.UUID;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testSyncUser_ExistingUser() {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn("keycloak-id-123");
        when(oidcUser.getEmail()).thenReturn("test@example.com");

        User existingUser = User.builder()
                .keycloakId("keycloak-id-123")
                .email("test@example.com")
                .role("client")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.syncUser(oidcUser);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void testUpdateUser() {
        UUID id = UUID.randomUUID();
        User existingUser = User.builder()
                .id(id)
                .firstName("Old")
                .lastName("Name")
                .keycloakId("kid")
                .build();

        User details = User.builder()
                .firstName("New")
                .lastName("Name")
                .role("freelancer")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.update(id, details);

        assertEquals("New", result.getFirstName());
        assertEquals("freelancer", result.getRole());
        verify(keycloakService).updateUser(eq("kid"), eq("New"), eq("Name"));
    }

    @Test
    void testCreateNotification() {
        UUID userId = UUID.randomUUID();
        Notification notif = Notification.builder()
                .userId(userId)
                .message("Hello")
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        Notification result = userService.createNotification(userId, "Hello", "INFO", "/url");

        assertNotNull(result);
        assertEquals("Hello", result.getMessage());
        verify(notificationRepository).save(any(Notification.class));
    }
}
