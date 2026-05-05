package tn.esprit.reservation.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.reservation.entities.Availability;
import tn.esprit.reservation.repositories.AvailabilityRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    public List<Availability> findAll() {
        return availabilityRepository.findByIsActiveTrue();
    }

    public List<Availability> findAllIncludingInactive() {
        return availabilityRepository.findAll();
    }

    public Optional<Availability> findById(Long id) {
        return availabilityRepository.findById(id);
    }

    public List<Availability> findByDate(LocalDate date) {
        return availabilityRepository.findByDateAndIsActiveTrue(date);
    }

    public List<Availability> findByDateRange(LocalDate start, LocalDate end) {
        return availabilityRepository.findByDateBetweenAndIsActiveTrue(start, end);
    }

    public List<Availability> searchByResource(String resourceName) {
        return availabilityRepository.findByResourceNameContainingIgnoreCaseAndIsActiveTrue(resourceName);
    }

    public List<Availability> findByFreelancerId(String freelancerId) {
        return availabilityRepository.findByFreelancerIdAndIsActiveTrue(freelancerId);
    }

    @Transactional
    public Availability create(Availability availability) {
        if (availability.getStartTime().isAfter(availability.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
        if (availability.getMaxSlots() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max slots must be positive");
        }
        Availability saved = availabilityRepository.save(availability);
        log.info("Created availability [id={}] for freelancer '{}' on {}", saved.getId(), saved.getFreelancerName(), saved.getDate());
        return saved;
    }

    @Transactional
    public Availability update(Long id, Availability updated) {
        return availabilityRepository.findById(id).map(existing -> {
            existing.setFreelancerId(updated.getFreelancerId());
            existing.setFreelancerName(updated.getFreelancerName());
            existing.setResourceName(updated.getResourceName());
            existing.setDescription(updated.getDescription());
            existing.setDate(updated.getDate());
            existing.setStartTime(updated.getStartTime());
            existing.setEndTime(updated.getEndTime());
            existing.setMaxSlots(updated.getMaxSlots());
            existing.setLocation(updated.getLocation());
            existing.setIsActive(updated.getIsActive());
            return availabilityRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Availability not found with id: " + id));
    }

    @Transactional
    public void delete(Long id) {
        availabilityRepository.findById(id).ifPresent(a -> {
            a.setIsActive(false);
            availabilityRepository.save(a);
            log.info("Soft-deleted availability id={}", id);
        });
    }
}
