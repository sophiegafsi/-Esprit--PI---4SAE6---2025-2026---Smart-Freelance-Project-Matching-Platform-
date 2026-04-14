package tn.esprit.GestionPortfolio.Entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be at most 120 characters")
    @Column(nullable = false, length = 120)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Completion date is required")
    @Column(nullable = false)
    private LocalDate completionDate;

    @NotNull(message = "Freelancer ID is required")
    @Positive(message = "Freelancer ID must be greater than 0")
    @Column(nullable = false)
    private Long freelancerId;

    @Builder.Default
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("achievement-skills")
    private List<AchievementSkill> achievementSkills = new ArrayList<>();

    @OneToOne(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("achievement-metric")
    private AchievementMetric achievementMetric;
}
