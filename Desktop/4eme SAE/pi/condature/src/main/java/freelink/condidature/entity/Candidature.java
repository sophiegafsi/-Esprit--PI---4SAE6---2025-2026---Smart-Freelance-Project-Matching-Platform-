package freelink.condidature.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidatures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID freelancerId;

    @ManyToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    @JsonBackReference("project-candidature")
    private Project project;

    @Column(name = "projectId")
    private UUID projectId;

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("candidature-contract")
    private Contract contract;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private String fileName;
    private String fileType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime applicationDate;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    @PrePersist
    protected void onCreate() {
        if (this.applicationDate == null) {
            this.applicationDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }
}
