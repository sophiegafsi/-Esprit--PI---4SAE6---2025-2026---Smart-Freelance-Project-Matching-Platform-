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
public class AchievementMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer complexityScore;

    private Integer impactScore;

    private Integer durationDays;

    @OneToOne
    @JoinColumn(name = "achievement_id")
    @JsonBackReference("achievement-metric")
    private Achievement achievement;

}