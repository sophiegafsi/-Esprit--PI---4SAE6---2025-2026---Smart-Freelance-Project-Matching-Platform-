package tn.esprit.GestionPortfolio.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long skillId;

    @Enumerated(EnumType.STRING)
    private ContributionLevel contributionLevel;

    private String usageDescription;

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    @JsonBackReference("achievement-skills")
    private Achievement achievement;

}