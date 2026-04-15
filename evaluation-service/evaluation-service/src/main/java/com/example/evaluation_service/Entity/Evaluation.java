package com.example.evaluation_service.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Score entre 1 et 5
    @Column(nullable = true)

    private Integer score;
    private String evaluatedUserEmail;
    // Commentaire
    @Column(length = 1000)
    private String comment;

    // Evaluation anonyme
    @Column(nullable = false)
    private boolean anonymous = false;

    // Date création
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date date;

    // Email utilisateur connecté
    @Column(name = "user_email")
    private String userEmail;

    // Date modification
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // Nom du projet
    @Column(nullable = false)
    private String projectName;

    // Evaluateur
    @Column(nullable = false)
    private String evaluatorName;

    // Utilisateur évalué
    @Column(nullable = false)
    private String evaluatedUserName;

    // Type d'évaluation
    @Enumerated(EnumType.STRING)
    private TypeEvaluation typeEvaluation;

    // Liste des reviews
    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Review> avis;

    // Avant insertion
    @PrePersist
    public void onCreate() {
        this.date = new Date();
        this.updatedAt = new Date();
    }

    // Avant modification
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = new Date();
    }
}