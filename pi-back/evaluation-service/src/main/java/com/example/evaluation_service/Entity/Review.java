package com.example.evaluation_service.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer score;  // note de 1 à 5

    @Column(length = 1000, nullable = false)
    private String comment;

    @Column(nullable = false)
    private String evaluatorName;  // nom de l'auteur

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;  // date de création
    @Column(name = "user_email")
    private String userEmail;
    @ManyToOne
    @JoinColumn(name = "evaluation_id")
    @JsonBackReference
    private Evaluation evaluation;
    @Column(length = 20)
    private String sentiment;
    @PrePersist
    public void onCreate() {
        if (this.date == null) this.date = new Date();
    }
}