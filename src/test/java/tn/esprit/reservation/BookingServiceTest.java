package tn.esprit.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.entities.Booking.BookingStatus;
import tn.esprit.reservation.repositories.AvailabilityRepository;
import tn.esprit.reservation.repositories.BookingRepository;
import tn.esprit.reservation.services.BookingService;
import tn.esprit.reservation.services.NotificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    // ──────────────────────── helpers ────────────────────────

    private Availability buildAvailability(Long id, boolean active, int maxSlots) {
        return Availability.builder()
                .id(id)
                .freelancerId("freelancer-1")
                .freelancerName("Alice")
                .resourceName("Design")
                .description("UI/UX session")
                .date(LocalDate.of(2025, 8, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .maxSlots(maxSlots)
                .location("Remote")
                .isActive(active)
                .build();
    }

    private Booking buildBooking(Long id, Long availabilityId, String userId, BookingStatus status) {
        return Booking.builder()
                .id(id)
                .availabilityId(availabilityId)
                .userId(userId)
                .userKeycloakId("kc-" + userId)
                .freelancerId("freelancer-1")
                .freelancerName("Alice")
                .userName("John Doe")
                .userEmail("john@example.com")
                .status(status)
                .notes("Test booking")
                .build();
    }

    // ══════════════════════════════════════════════════════════
    // findAll
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("returns all bookings from repository")
        void shouldReturnAllBookings() {
            Booking b1 = buildBooking(1L, 10L, "user-1", BookingStatus.PENDING);
            Booking b2 = buildBooking(2L, 10L, "user-2", BookingStatus.CONFIRMED);
            when(bookingRepository.findAll()).thenReturn(List.of(b1, b2));

            assertThat(bookingService.findAll()).hasSize(2).containsExactly(b1, b2);
        }

        @Test
        @DisplayName("returns empty list when no bookings")
        void shouldReturnEmptyListWhenNone() {
            when(bookingRepository.findAll()).thenReturn(List.of());
            assertThat(bookingService.findAll()).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════
    // findById
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("returns booking when it exists")
        void shouldReturnBookingWhenFound() {
            Booking b = buildBooking(5L, 10L, "user-1", BookingStatus.PENDING);
            when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));

            assertThat(bookingService.findById(5L)).isPresent().contains(b);
        }

        @Test
        @DisplayName("returns empty Optional when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
            assertThat(bookingService.findById(99L)).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════
    // findByUserId / findByFreelancer / findByFreelancerId
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Query-by-field methods")
    class QueryByField {

        @Test
        @DisplayName("findByUserId delegates correctly")
        void shouldFindByUserId() {
            Booking b = buildBooking(1L, 10L, "user-abc", BookingStatus.CONFIRMED);
            when(bookingRepository.findByUserId("user-abc")).thenReturn(List.of(b));

            assertThat(bookingService.findByUserId("user-abc")).containsExactly(b);
        }

        @Test
        @DisplayName("findByFreelancer delegates correctly")
        void shouldFindByFreelancerName() {
            Booking b = buildBooking(2L, 10L, "user-1", BookingStatus.PENDING);
            when(bookingRepository.findByFreelancerName("Alice")).thenReturn(List.of(b));

            assertThat(bookingService.findByFreelancer("Alice")).containsExactly(b);
        }

        @Test
        @DisplayName("findByFreelancerId delegates correctly")
        void shouldFindByFreelancerId() {
            Booking b = buildBooking(3L, 10L, "user-1", BookingStatus.PENDING);
            when(bookingRepository.findByFreelancerId("freelancer-1")).thenReturn(List.of(b));

            assertThat(bookingService.findByFreelancerId("freelancer-1")).containsExactly(b);
        }

        @Test
        @DisplayName("findByAvailabilityId delegates correctly")
        void shouldFindByAvailabilityId() {
            Booking b = buildBooking(4L, 10L, "user-1", BookingStatus.CONFIRMED);
            when(bookingRepository.findByAvailabilityId(10L)).thenReturn(List.of(b));

            assertThat(bookingService.findByAvailabilityId(10L)).containsExactly(b);
        }
    }

    // ══════════════════════════════════════════════════════════
    // create — happy path
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create() — happy path")
    class CreateSuccess {

        @Test
        @DisplayName("creates booking, sets freelancer info, status PENDING, sends notification")
        void shouldCreateBookingSuccessfully() {
            Availability av = buildAvailability(10L, true, 5);
            Booking input   = buildBooking(null, 10L, "user-1", BookingStatus.PENDING);
            Booking saved   = buildBooking(1L,  10L, "user-1", BookingStatus.PENDING);

            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));
            when(bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(10L, "user-1", BookingStatus.CANCELLED))
                    .thenReturn(false);
            when(bookingRepository.countActiveBookingsByAvailabilityId(10L)).thenReturn(2L);
            when(bookingRepository.save(input)).thenReturn(saved);

            Booking result = bookingService.create(input);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
            assertThat(input.getFreelancerName()).isEqualTo("Alice");
            assertThat(input.getFreelancerId()).isEqualTo("freelancer-1");

            verify(bookingRepository).save(input);
            verify(notificationService).notifyFreelancerOfNewRequest(saved, av);
        }
    }

    // ══════════════════════════════════════════════════════════
    // create — guard conditions
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create() — validation guards")
    class CreateGuards {

        @Test
        @DisplayName("throws 404 when availability does not exist")
        void shouldThrow404WhenAvailabilityNotFound() {
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());
            Booking input = buildBooking(null, 99L, "user-1", BookingStatus.PENDING);

            assertThatThrownBy(() -> bookingService.create(input))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND))
                    .hasMessageContaining("Availability not found");
        }

        @Test
        @DisplayName("throws 400 when availability is inactive")
        void shouldThrow400WhenAvailabilityInactive() {
            Availability inactive = buildAvailability(10L, false, 5);
            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(inactive));
            Booking input = buildBooking(null, 10L, "user-1", BookingStatus.PENDING);

            assertThatThrownBy(() -> bookingService.create(input))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST))
                    .hasMessageContaining("no longer active");
        }

        @Test
        @DisplayName("throws 400 when freelancer tries to book their own slot")
        void shouldThrow400WhenFreelancerBooksOwn() {
            Availability av = buildAvailability(10L, true, 5); // freelancerId = "freelancer-1"
            // userId == freelancerId  → self-booking
            Booking input = buildBooking(null, 10L, "freelancer-1", BookingStatus.PENDING);
            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));

            assertThatThrownBy(() -> bookingService.create(input))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST))
                    .hasMessageContaining("cannot book their own");
        }

        @Test
        @DisplayName("throws 409 when user already booked the same slot")
        void shouldThrow409WhenDoubleBooking() {
            Availability av = buildAvailability(10L, true, 5);
            Booking input   = buildBooking(null, 10L, "user-1", BookingStatus.PENDING);

            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));
            when(bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(10L, "user-1", BookingStatus.CANCELLED))
                    .thenReturn(true);

            assertThatThrownBy(() -> bookingService.create(input))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT))
                    .hasMessageContaining("already booked");
        }

        @Test
        @DisplayName("throws 409 when availability is at full capacity")
        void shouldThrow409WhenNoSlotsLeft() {
            Availability av = buildAvailability(10L, true, 2); // maxSlots = 2
            Booking input   = buildBooking(null, 10L, "user-1", BookingStatus.PENDING);

            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));
            when(bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(10L, "user-1", BookingStatus.CANCELLED))
                    .thenReturn(false);
            when(bookingRepository.countActiveBookingsByAvailabilityId(10L)).thenReturn(2L); // full!
            when(availabilityRepository.findByFreelancerNameContainingIgnoreCaseAndIsActiveTrue("Alice"))
                    .thenReturn(List.of()); // no alternatives

            assertThatThrownBy(() -> bookingService.create(input))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT))
                    .hasMessageContaining("Maximum capacity");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("includes alternative slot suggestions in capacity error message")
        void shouldIncludeAlternativeSuggestionsWhenFull() {
            Availability av = buildAvailability(10L, true, 1); // maxSlots = 1, full

            // Alternative slot that still has room
            Availability alternative = buildAvailability(20L, true, 3);
            alternative.setDate(LocalDate.now().plusDays(2));

            Booking input = buildBooking(null, 10L, "user-1", BookingStatus.PENDING);

            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));
            when(bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(10L, "user-1", BookingStatus.CANCELLED))
                    .thenReturn(false);
            when(bookingRepository.countActiveBookingsByAvailabilityId(10L)).thenReturn(1L); // full
            when(availabilityRepository.findByFreelancerNameContainingIgnoreCaseAndIsActiveTrue("Alice"))
                    .thenReturn(List.of(alternative));
            when(bookingRepository.countActiveBookingsByAvailabilityId(20L)).thenReturn(0L); // available

            assertThatThrownBy(() -> bookingService.create(input))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Suggested options");
        }
    }

    // ══════════════════════════════════════════════════════════
    // cancel
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("sets status to CANCELLED and sends notification")
        void shouldCancelBookingSuccessfully() {
            Booking booking = buildBooking(1L, 10L, "user-1", BookingStatus.CONFIRMED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(booking)).thenReturn(booking);

            Booking result = bookingService.cancel(1L);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            verify(notificationService).notifyClientOfBookingUpdate(booking, "BOOKING_CANCELLED");
        }

        @Test
        @DisplayName("throws 400 when booking is already cancelled")
        void shouldThrow400WhenAlreadyCancelled() {
            Booking booking = buildBooking(1L, 10L, "user-1", BookingStatus.CANCELLED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancel(1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST))
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("throws 404 when booking does not exist")
        void shouldThrow404WhenBookingNotFound() {
            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancel(99L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // ══════════════════════════════════════════════════════════
    // confirm
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("confirm()")
    class Confirm {

        @Test
        @DisplayName("sets status to CONFIRMED and sends notification")
        void shouldConfirmBookingSuccessfully() {
            Booking booking = buildBooking(1L, 10L, "user-1", BookingStatus.PENDING);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(booking)).thenReturn(booking);

            Booking result = bookingService.confirm(1L);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            verify(notificationService).notifyClientOfBookingUpdate(booking, "BOOKING_CONFIRMED");
        }

        @Test
        @DisplayName("throws 404 when booking does not exist")
        void shouldThrow404WhenNotFound() {
            when(bookingRepository.findById(55L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.confirm(55L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // ══════════════════════════════════════════════════════════
    // delete
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deletes booking when it exists")
        void shouldDeleteBookingSuccessfully() {
            when(bookingRepository.existsById(1L)).thenReturn(true);

            bookingService.delete(1L);

            verify(bookingRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws 404 when booking does not exist")
        void shouldThrow404WhenNotFound() {
            when(bookingRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> bookingService.delete(99L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(bookingRepository, never()).deleteById(any());
        }
    }

    // ══════════════════════════════════════════════════════════
    // countAvailableSlots
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("countAvailableSlots()")
    class CountAvailableSlots {

        @Test
        @DisplayName("returns remaining slots correctly")
        void shouldReturnCorrectRemainingSlots() {
            Availability av = buildAvailability(10L, true, 5);
            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));
            when(bookingRepository.countActiveBookingsByAvailabilityId(10L)).thenReturn(3L);

            assertThat(bookingService.countAvailableSlots(10L)).isEqualTo(2L);
        }

        @Test
        @DisplayName("returns 0 when slot is fully booked (no negative values)")
        void shouldReturnZeroWhenFull() {
            Availability av = buildAvailability(10L, true, 3);
            when(availabilityRepository.findById(10L)).thenReturn(Optional.of(av));
            when(bookingRepository.countActiveBookingsByAvailabilityId(10L)).thenReturn(3L);

            assertThat(bookingService.countAvailableSlots(10L)).isEqualTo(0L);
        }

        @Test
        @DisplayName("throws 404 when availability not found")
        void shouldThrow404WhenNotFound() {
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.countAvailableSlots(99L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }
}
