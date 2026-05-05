package com.example.projet_service.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "projet_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjetDetaille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskname;

    @Column(length = 500)
    private String description;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    @JsonIgnore
    private Projet projet;
}
