package tn.esprit.reservation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.reservation.entities.Availability;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByIsActiveTrue();

    List<Availability> findByDateAndIsActiveTrue(LocalDate date);

    List<Availability> findByDateBetweenAndIsActiveTrue(LocalDate start, LocalDate end);

    List<Availability> findByResourceNameContainingIgnoreCaseAndIsActiveTrue(String resourceName);

    List<Availability> findByFreelancerIdAndIsActiveTrue(String freelancerId);

    List<Availability> findByFreelancerNameContainingIgnoreCaseAndIsActiveTrue(String freelancerName);
}
