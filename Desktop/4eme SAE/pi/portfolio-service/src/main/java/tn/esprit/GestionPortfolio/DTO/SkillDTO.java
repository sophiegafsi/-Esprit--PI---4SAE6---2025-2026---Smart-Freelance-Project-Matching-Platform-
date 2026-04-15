package tn.esprit.GestionPortfolio.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDTO {
    private Long id;
    private String name;
    private String level;
    private Integer yearsOfExperience;
    private String description;
}