package com.example.reclamation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DuplicateCheckResponseDTO {
    private Integer idReclamation;
    private String sujet;
    private String description;
    private double similarityScore;
}