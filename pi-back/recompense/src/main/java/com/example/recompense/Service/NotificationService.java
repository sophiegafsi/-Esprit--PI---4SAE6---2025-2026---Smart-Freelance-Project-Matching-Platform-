package com.example.recompense.Service;

import com.example.recompense.Entity.Notification;
import com.example.recompense.Repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(String email, String message) {
        createAndBroadcast(email, message);
    }

    public List<Notification> getUserNotifications(String email) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    public Notification create(String email, String message) {
        Notification notification = buildNotification(email, message);
        return notificationRepository.save(notification);
    }

    public Notification createAndBroadcast(String email, String message) {
        Notification notification = buildNotification(email, message);
        Notification saved = notificationRepository.save(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + email, saved);
        return saved;
    }

    private Notification buildNotification(String email, String message) {
        Notification notification = new Notification();
        notification.setUserEmail(email);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
