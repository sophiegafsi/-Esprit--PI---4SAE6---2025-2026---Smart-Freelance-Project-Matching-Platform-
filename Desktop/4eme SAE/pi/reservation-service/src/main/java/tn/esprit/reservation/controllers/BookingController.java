package tn.esprit.reservation.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import tn.esprit.reservation.entities.Booking;
import tn.esprit.reservation.services.BookingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@PathVariable Long id) {
        return bookingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(bookingService.findByUserId(userId));
    }

    @GetMapping("/freelancer/{freelancerName}")
    public ResponseEntity<List<Booking>> getByFreelancer(@PathVariable String freelancerName) {
        return ResponseEntity.ok(bookingService.findByFreelancer(freelancerName));
    }

    @GetMapping("/freelancer")
    public ResponseEntity<List<Booking>> getMyRequests(@AuthenticationPrincipal Jwt jwt) {
        String freelancerId = jwt.getSubject(); // This is the Keycloak sub (ID)
        return ResponseEntity.ok(bookingService.findByFreelancerId(freelancerId));
    }

    @GetMapping("/availability/{availabilityId}")
    public ResponseEntity<List<Booking>> getByAvailability(@PathVariable Long availabilityId) {
        return ResponseEntity.ok(bookingService.findByAvailabilityId(availabilityId));
    }

    @GetMapping("/availability/{availabilityId}/slots")
    public ResponseEntity<Map<String, Long>> getAvailableSlots(@PathVariable Long availabilityId) {
        long slots = bookingService.countAvailableSlots(availabilityId);
        return ResponseEntity.ok(Map.of("availableSlots", slots));
    }

    @PostMapping
    public ResponseEntity<Booking> create(@RequestBody Booking booking, @AuthenticationPrincipal Jwt jwt) {
        // Automatically link the booking to the authenticated user's Keycloak ID for notifications
        booking.setUserKeycloakId(jwt.getSubject()); 
        Booking created = bookingService.create(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancel(id));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirm(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
