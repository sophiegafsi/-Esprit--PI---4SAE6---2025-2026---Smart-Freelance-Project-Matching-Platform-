package tn.esprit.reservation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.reservation.entities.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByReferenceId(Long referenceId);

    long countByUserIdAndIsReadFalse(String userId);
}
