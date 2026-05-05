package freelink.condidature.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "candidatureId", insertable = false, updatable = false)
    @JsonBackReference("candidature-contract")
    private Candidature candidature;

    @Column(name = "candidatureId")
    private UUID candidatureId;

    @Column(name = "projectId")
    private Long projectId;

    private UUID clientId;
    private UUID freelancerId;

    @Column(columnDefinition = "TEXT")
    private String terms;

    private Double hourlyRate; // Added for hourly tracking

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    @Column(columnDefinition = "LONGTEXT")
    private String clientSignature;

    @Column(columnDefinition = "LONGTEXT")
    private String freelancerSignature;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private ContractStatus status;

    public enum ContractStatus {
        PENDING,
        ONESIDED,
        COMPLETED,
        ABORTED
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ContractStatus.PENDING;
        }
    }
}
