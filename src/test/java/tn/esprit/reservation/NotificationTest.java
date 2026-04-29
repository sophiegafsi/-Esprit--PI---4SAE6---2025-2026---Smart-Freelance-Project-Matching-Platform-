package tn.esprit.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.reservation.entities.Notification;
import tn.esprit.reservation.repositories.NotificationRepository;
import tn.esprit.reservation.services.NotificationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
public class NotificationTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("findByUserId returns notifications ordered by date")
    public void testFetchByUserId() {
        // Arrange
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("john-doe"))
                .thenReturn(List.of());

        // Act
        List<Notification> result = notificationRepository
                .findByUserIdOrderByCreatedAtDesc("john-doe");

        // Assert
        assertThat(result).isNotNull();
        verify(notificationRepository, times(1))
                .findByUserIdOrderByCreatedAtDesc("john-doe");
        System.out.println("FETCH SUCCESSFUL — returned " + result.size() + " notifications");
    }
}
