package tn.esprit.reservation.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.repositories.AvailabilityRepository;
import tn.esprit.reservation.repositories.BookingRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8082/api/users";

    /**
     * Send notification via User Service REST API
     */
    private void sendGlobalNotification(String userId, String message, String type, String actionUrl) {
        try {
            String url = USER_SERVICE_URL + "/notifications/by-keycloak/" + userId;
            Map<String, String> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("type", type);
            payload.put("actionUrl", actionUrl);

            restTemplate.postForEntity(url, payload, Void.class);
            log.info("Successfully sent global notification to user '{}' (keycloak) via user-service", userId);
        } catch (Exception e) {
            log.error("Failed to send global notification to user '{}': {}", userId, e.getMessage());
        }
    }

    public void notifyFreelancerOfNewRequest(Booking booking, Availability availability) {
        String msg = "New booking request from " + booking.getUserName() + " for " + availability.getDate() + " at " + availability.getStartTime();
        sendGlobalNotification(
            availability.getFreelancerId(), 
            msg, 
            "NEW_BOOKING", 
            "/my-availabilities"
        );
    }

    public void notifyClientOfBookingUpdate(Booking booking, String updateType) {
        availabilityRepository.findById(booking.getAvailabilityId()).ifPresent(availability -> {
            String freelancerName = availability.getFreelancerName();
            String msg = updateType.contains("CONFIRMED")
                    ? "Great news! Freelancer " + freelancerName + " confirmed your booking for " + availability.getDate() + "."
                    : "Update: Freelancer " + freelancerName + " declined/cancelled your booking for " + availability.getDate() + ".";

            sendGlobalNotification(
                booking.getUserKeycloakId(), 
                msg, 
                updateType, 
                "/my-bookings"
            );
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

                // 24-hour reminder condition
                if (minutesUntil > 0 && minutesUntil <= 1440 && !booking.getReminder24hSent()) {
                    sendReminder(booking, availability, "Reminder: Your session is in less than 24 hours!", "REMINDER_24H");
                    booking.setReminder24hSent(true);
                    bookingRepository.save(booking);
                }

                // 1-hour reminder condition
                if (minutesUntil > 0 && minutesUntil <= 60 && !booking.getReminder1hSent()) {
                    sendReminder(booking, availability, "Urgent Reminder: Your session starts in less than 1 hour!", "REMINDER_1H");
                    booking.setReminder1hSent(true);
                    bookingRepository.save(booking);
                }
            });
        }
    }

    private void sendReminder(Booking booking, Availability availability, String msg, String type) {
        // Notify Client
        sendGlobalNotification(booking.getUserKeycloakId(), msg, type, "/my-bookings");

        // Notify Freelancer
        sendGlobalNotification(
            availability.getFreelancerId(), 
            msg + " (Client: " + booking.getUserName() + ")", 
            type, 
            "/my-availabilities"
        );
    }

    public void markRelatedNotificationsAsRead(Long referenceId) {
        // Since notifications are handled by user-service, we don't handle marking as read here
        // The user will mark them as read in the navbar.
    }
}
