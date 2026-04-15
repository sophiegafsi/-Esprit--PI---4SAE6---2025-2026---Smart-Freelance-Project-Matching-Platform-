package tn.esprit.reservation.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.entities.Booking.BookingStatus;
import tn.esprit.reservation.repositories.AvailabilityRepository;
import tn.esprit.reservation.repositories.BookingRepository;
import tn.esprit.reservation.services.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationService notificationService;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> findByUserId(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> findByFreelancer(String freelancerName) {
        return bookingRepository.findByFreelancerName(freelancerName);
    }

    public List<Booking> findByAvailabilityId(Long availabilityId) {
        return bookingRepository.findByAvailabilityId(availabilityId);
    }

    /**
     * Creates a new booking with double-booking and capacity conflict detection.
     */
    @Transactional
    public Booking create(Booking booking) {
        Long availabilityId = booking.getAvailabilityId();

        // 1. Check availability exists and is active
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Availability not found with id: " + availabilityId));

        if (!availability.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This availability slot is no longer active");
        }

        // Prevent freelancers from booking themselves
        if (booking.getUserId().equalsIgnoreCase(availability.getFreelancerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Freelancers cannot book their own availabilities.");
        }

        // 2. Prevent double-booking: same user cannot book the same slot twice
        boolean alreadyBooked = bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(
                availabilityId, booking.getUserId(), BookingStatus.CANCELLED);
        if (alreadyBooked) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User '" + booking.getUserId() + "' has already booked this availability slot");
        }

        // 3. Check capacity: count active (non-cancelled) bookings vs maxSlots
        long activeBookings = bookingRepository.countActiveBookingsByAvailabilityId(availabilityId);
        if (activeBookings >= availability.getMaxSlots()) {
            // Retrieve alternative suggestions for the user
            List<Availability> alternatives = availabilityRepository.findByFreelancerNameContainingIgnoreCaseAndIsActiveTrue(availability.getFreelancerName())
                    .stream()
                    .filter(a -> a.getDate().isAfter(LocalDate.now().minusDays(1)))
                    .filter(a -> !a.getId().equals(availabilityId))
                    .filter(a -> bookingRepository.countActiveBookingsByAvailabilityId(a.getId()) < a.getMaxSlots())
                    .limit(3)
                    .toList();

            StringBuilder conflictMsg = new StringBuilder("No slots available. Maximum capacity (")
                    .append(availability.getMaxSlots()).append(") reached.");

            if (!alternatives.isEmpty()) {
                conflictMsg.append(" Suggested options: ");
                alternatives.forEach(a -> conflictMsg.append(String.format(" ID %d on %s at %s;", a.getId(), a.getDate(), a.getStartTime())));
            }

            throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMsg.toString());
        }

        booking.setFreelancerId(availability.getFreelancerId());
        booking.setFreelancerName(availability.getFreelancerName());
        booking.setStatus(BookingStatus.PENDING);
        Booking saved = bookingRepository.save(booking);
        log.info("Created booking [id={}] for user '{}' on availability [id={}]. Slots used: {}/{}",
                saved.getId(), saved.getUserId(), availabilityId, activeBookings + 1, availability.getMaxSlots());
        
        // Notify the freelancer!
        notificationService.notifyFreelancerOfNewRequest(saved, availability);
        
        return saved;
    }

    /**
     * Cancels a booking (soft cancel – sets status to CANCELLED).
     */
    @Transactional
    public Booking cancel(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already cancelled");
            }
            booking.setStatus(BookingStatus.CANCELLED);
            Booking saved = bookingRepository.save(booking);
            log.info("Cancelled booking [id={}] for user '{}'", saved.getId(), saved.getUserId());
            notificationService.notifyClientOfBookingUpdate(saved, "BOOKING_CANCELLED");
            return saved;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found with id: " + id));
    }

    /**
     * Confirm a pending booking.
     */
    @Transactional
    public Booking confirm(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(BookingStatus.CONFIRMED);
            Booking saved = bookingRepository.save(booking);
            notificationService.notifyClientOfBookingUpdate(saved, "BOOKING_CONFIRMED");
            return saved;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found with id: " + id));
    }

    @Transactional
    public void delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    public long countAvailableSlots(Long availabilityId) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found"));
        long activeBookings = bookingRepository.countActiveBookingsByAvailabilityId(availabilityId);
        return Math.max(0, availability.getMaxSlots() - activeBookings);
    }
}
