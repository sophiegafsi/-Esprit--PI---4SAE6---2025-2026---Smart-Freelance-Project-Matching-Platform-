package com.example.evaluation_service.Entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Historique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String action;


    private String entite;


    private Long entiteId;


    private String utilisateur;


    private LocalDateTime dateAction;


    private String commentaire;
}
