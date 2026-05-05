package tn.esprit.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.reservation.repositories.NotificationRepository;

@SpringBootTest
public class NotificationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    public void testFetch() {
        System.out.println("TESTING FETCH");
        notificationRepository.findByUserIdOrderByCreatedAtDesc("John doe");
        System.out.println("FETCH SUCCESSFUL");
    }
}
