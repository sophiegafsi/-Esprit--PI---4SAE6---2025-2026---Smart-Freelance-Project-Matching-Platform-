package freelink.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {

    @Id
    private UUID id;

    private String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // client, freelancer, admin

    public void setRole(String role) {
        System.out.println(">>> User.setRole called for email [" + this.email + "]: " + this.role + " -> " + role);
        if (role == null || role.isEmpty()) {
            Thread.dumpStack();
        }
        this.role = role;
    }

    private java.time.LocalDate birthDate;
    private String country;

    // Freelancer Attributes
    private String jobTitle;
    private String bio;
    private String skills; // Comma separated for now
    private Double hourlyRate;
    private String portfolioUrl;

    @jakarta.persistence.Lob
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String imageUrl;

    private LocalDateTime createdAt;
}
