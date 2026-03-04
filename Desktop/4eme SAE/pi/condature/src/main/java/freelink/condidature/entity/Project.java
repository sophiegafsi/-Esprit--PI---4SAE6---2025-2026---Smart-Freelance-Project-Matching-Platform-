package freelink.condidature.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    private UUID id;

    private String title;
    private String description;
    private UUID clientId;
    private Double budget;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private ProjectStatus status;

    public enum ProjectStatus {
        OPEN,
        CLOSED
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ProjectStatus.OPEN;
        }
    }

    @jakarta.persistence.OneToMany(mappedBy = "project", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    @JsonManagedReference("project-candidature")
    private java.util.List<Candidature> candidatures;

    @jakarta.persistence.OneToMany(mappedBy = "project", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    @JsonManagedReference("project-contract")
    private java.util.List<Contract> contracts;
}
