package tn.esprit.gestionskills.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillScoreDto {
    private Long skillId;
    private String name;
    private Integer score;
}