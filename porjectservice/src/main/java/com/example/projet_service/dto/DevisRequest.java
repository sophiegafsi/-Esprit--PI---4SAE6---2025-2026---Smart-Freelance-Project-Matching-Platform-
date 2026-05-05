package com.example.projet_service.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DevisRequest {
    private Long projetId;       // devis basé sur un projet existant
    private String deadline;     // optionnel (yyyy-MM-dd)
}