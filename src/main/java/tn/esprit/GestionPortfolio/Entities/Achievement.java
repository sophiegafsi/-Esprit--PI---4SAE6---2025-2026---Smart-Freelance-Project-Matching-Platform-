package tn.esprit.GestionPortfolio.Entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    private String title;

    private String description;

    private LocalDate completionDate;

    private Long freelancerId;

    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("achievement-skills")
    private List<AchievementSkill> achievementSkills = new ArrayList<>();

    @OneToOne(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("achievement-metric")
    private AchievementMetric achievementMetric;
}