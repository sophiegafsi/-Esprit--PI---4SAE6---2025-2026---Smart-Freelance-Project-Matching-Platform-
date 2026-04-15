package tn.esprit.reservation.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long availabilityId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String userKeycloakId; // The Keycloak sub (ID) for notifications

    @Column(nullable = false)
    private String freelancerId;

    @Column(nullable = false)
    private String freelancerName;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean reminder1hSent = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean reminder24hSent = false;

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
