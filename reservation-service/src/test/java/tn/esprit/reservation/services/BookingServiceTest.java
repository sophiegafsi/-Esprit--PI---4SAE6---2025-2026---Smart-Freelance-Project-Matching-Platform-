package tn.esprit.reservation.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.repositories.AvailabilityRepository;
import tn.esprit.reservation.repositories.BookingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("Should create booking when availability is active and has capacity")
    void testCreateBookingSuccess() {
        // Arrange
        Booking booking = new Booking();
        booking.setAvailabilityId(1L);
        booking.setUserId("client-1");

        Availability availability = new Availability();
        availability.setId(1L);
        availability.setIsActive(true);
        availability.setMaxSlots(5);
        availability.setFreelancerId("freelancer-1");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(eq(1L), eq("client-1"), any())).thenReturn(false);
        when(bookingRepository.countActiveBookingsByAvailabilityId(1L)).thenReturn(2L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Booking saved = bookingService.create(booking);

        // Assert
        assertNotNull(saved);
        assertEquals(Booking.BookingStatus.PENDING, saved.getStatus());
        verify(notificationService).notifyFreelancerOfNewRequest(any(), any());
    }

    @Test
    @DisplayName("Should throw CONFLICT when user already booked the same slot")
    void testCreateBookingDoubleBooking() {
        // Arrange
        Booking booking = new Booking();
        booking.setAvailabilityId(1L);
        booking.setUserId("client-1");

        Availability availability = new Availability();
        availability.setId(1L);
        availability.setIsActive(true);
        availability.setFreelancerId("freelancer-1");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(bookingRepository.existsByAvailabilityIdAndUserIdAndStatusNot(eq(1L), eq("client-1"), any())).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> bookingService.create(booking));
    }

    @Test
    @DisplayName("Should throw CONFLICT when maximum capacity is reached")
    void testCreateBookingCapacityReached() {
        // Arrange
        Booking booking = new Booking();
        booking.setAvailabilityId(1L);
        booking.setUserId("client-2");

        Availability availability = new Availability();
        availability.setId(1L);
        availability.setIsActive(true);
        availability.setMaxSlots(2);
        availability.setFreelancerId("freelancer-1");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(bookingRepository.countActiveBookingsByAvailabilityId(1L)).thenReturn(2L); // Already at 2/2

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> bookingService.create(booking));
        assertTrue(ex.getReason().contains("Maximum capacity"));
    }

    @Test
    void testConfirmBooking_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        
        Booking result = bookingService.confirm(1L);
        
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());
        verify(notificationService).notifyClientOfBookingUpdate(any(), eq("BOOKING_CONFIRMED"));
    }

    @Test
    void testCancelBooking_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        
        Booking result = bookingService.cancel(1L);
        
        assertEquals(Booking.BookingStatus.CANCELLED, result.getStatus());
        verify(notificationService).notifyClientOfBookingUpdate(any(), eq("BOOKING_CANCELLED"));
    }
}
