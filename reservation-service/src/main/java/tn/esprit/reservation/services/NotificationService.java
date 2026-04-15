package tn.esprit.reservation.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.entities.Notification;
import tn.esprit.reservation.repositories.AvailabilityRepository;
import tn.esprit.reservation.repositories.BookingRepository;
import tn.esprit.reservation.repositories.NotificationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationRepository notificationRepository;

   
    public void notifyFreelancerOfNewRequest(Booking booking, Availability availability) {
        String freelancerId = availability.getResourceName(); // Using resourceName as freelancer user ID placeholder

        Notification notification = Notification.builder()
                .userId(freelancerId)
                .type(Notification.NotificationType.NEW_BOOKING)
                .message("You have a new booking request from " + booking.getUserName() + " for " + availability.getDate() + " at " + availability.getStartTime())
                .referenceId(booking.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Saved NEW_BOOKING notification for freelancer '{}'", freelancerId);
    }

   
    public void notifyClientOfBookingUpdate(Booking booking, Notification.NotificationType type) {
        availabilityRepository.findById(booking.getAvailabilityId()).ifPresent(availability -> {
            String freelancerId = availability.getResourceName();
            String msg = type == Notification.NotificationType.BOOKING_CONFIRMED
                    ? "Great news! Freelancer " + freelancerId + " confirmed your booking for " + availability.getDate() + "."
                    : "Update: Freelancer " + freelancerId + " declined/cancelled your booking for " + availability.getDate() + ".";

            Notification notification = Notification.builder()
                    .userId(booking.getUserId())
                    .type(type)
                    .message(msg)
                    .referenceId(booking.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
            log.info("Saved {} notification for client '{}'", type, booking.getUserId());
        });
    }

    
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processReminders() {
        log.info("Running NotificationService.processReminders() cron job...");
        List<Booking> pendingBookings = bookingRepository.findBookingsNeedingReminders();

        for (Booking booking : pendingBookings) {
            availabilityRepository.findById(booking.getAvailabilityId()).ifPresent(availability -> {
                LocalDateTime startDateTime = LocalDateTime.of(availability.getDate(), availability.getStartTime());
                long minutesUntil = ChronoUnit.MINUTES.between(LocalDateTime.now(), startDateTime);

                // 24-hour reminder condition: between 23h and 24h away (1380 to 1440 mins)
                if (minutesUntil > 0 && minutesUntil <= 1440 && !booking.getReminder24hSent()) {
                    createReminderNotification(booking, availability, "Reminder: Your session is in less than 24 hours!", Notification.NotificationType.REMINDER_24H);
                    booking.setReminder24hSent(true);
                    bookingRepository.save(booking);
                }

                // 1-hour reminder condition: between 0h and 1h away (0 to 60 mins)
                if (minutesUntil > 0 && minutesUntil <= 60 && !booking.getReminder1hSent()) {
                    createReminderNotification(booking, availability, "Urgent Reminder: Your session starts in less than 1 hour!", Notification.NotificationType.REMINDER_1H);
                    booking.setReminder1hSent(true);
                    bookingRepository.save(booking);
                }
            });
        }
    }

    private void createReminderNotification(Booking booking, Availability availability, String msg, Notification.NotificationType type) {
        // Notify Client
        Notification clientNotif = Notification.builder()
                .userId(booking.getUserId())
                .type(type)
                .message(msg)
                .referenceId(booking.getId())
                .build();
        notificationRepository.save(clientNotif);

        // Notify Freelancer
        Notification freelancerNotif = Notification.builder()
                .userId(availability.getResourceName())
                .type(type)
                .message(msg + " (Client: " + booking.getUserName() + ")")
                .referenceId(booking.getId())
                .build();
        notificationRepository.save(freelancerNotif);


        log.info("Sent {} to client '{}' and freelancer '{}' for booking ID: {}", type, booking.getUserId(), availability.getResourceName(), booking.getId());
    }

    @Transactional
    public void markRelatedNotificationsAsRead(Long referenceId) {
        List<Notification> notifications = notificationRepository.findByReferenceId(referenceId);
        for (Notification notif : notifications) {
            notif.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
        log.info("Marked {} notifications as read for referenceId: {}", notifications.size(), referenceId);
    }
}
