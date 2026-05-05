package tn.esprit.reservation.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.services.AvailabilityService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<Availability>> getAll() {
        return ResponseEntity.ok(availabilityService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Availability> getById(@PathVariable Long id) {
        return availabilityService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Availability>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.findByDate(date));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Availability>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(availabilityService.findByDateRange(start, end));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Availability>> search(@RequestParam String resourceName) {
        return ResponseEntity.ok(availabilityService.searchByResource(resourceName));
    }

    /** Get all availabilities for a specific freelancer (by their Keycloak user ID) */
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<Availability>> getByFreelancer(@PathVariable String freelancerId) {
        return ResponseEntity.ok(availabilityService.findByFreelancerId(freelancerId));
    }

    /** Get the authenticated freelancer's own availabilities */
    @GetMapping("/my")
    public ResponseEntity<List<Availability>> getMyAvailabilities(@AuthenticationPrincipal Jwt jwt) {
        String freelancerId = jwt.getSubject();
        return ResponseEntity.ok(availabilityService.findByFreelancerId(freelancerId));
    }

    /** Create a new availability slot — freelancer's ID and name are extracted from the JWT */
    @PostMapping
    public ResponseEntity<Availability> create(
            @RequestBody Availability availability,
            @AuthenticationPrincipal Jwt jwt) {
        availability.setFreelancerId(jwt.getSubject());
        String name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("preferred_username");
        }
        availability.setFreelancerName(name != null ? name : jwt.getSubject());
        Availability created = availabilityService.create(availability);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Availability> update(@PathVariable Long id, @RequestBody Availability availability) {
        try {
            return ResponseEntity.ok(availabilityService.update(id, availability));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        availabilityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
