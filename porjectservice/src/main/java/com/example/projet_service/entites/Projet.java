package com.example.projet_service.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private Domaine domaine;

    private UUID clientId;
    
    private Double budget;
    
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'OPEN'")
    private String status = "OPEN";

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProjetDetaille> details = new ArrayList<>();
}
