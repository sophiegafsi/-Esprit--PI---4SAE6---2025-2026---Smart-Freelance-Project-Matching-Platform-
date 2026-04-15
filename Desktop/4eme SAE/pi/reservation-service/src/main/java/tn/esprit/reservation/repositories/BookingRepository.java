package tn.esprit.reservation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.entities.Booking.BookingStatus;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByAvailabilityId(Long availabilityId);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.availabilityId = :availabilityId AND b.status <> 'CANCELLED'")
    long countActiveBookingsByAvailabilityId(@Param("availabilityId") Long availabilityId);

    boolean existsByAvailabilityIdAndUserIdAndStatusNot(Long availabilityId, String userId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND (b.reminder1hSent = false OR b.reminder24hSent = false)")
    List<Booking> findBookingsNeedingReminders();

    List<Booking> findByFreelancerId(String freelancerId);

    List<Booking> findByFreelancerName(String freelancerName);
}
