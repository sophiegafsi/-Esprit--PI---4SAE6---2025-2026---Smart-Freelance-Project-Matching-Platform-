package com.example.recompense.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data

public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String message;

    @Column(name = "is_read")
    private boolean read;

    private LocalDateTime createdAt = LocalDateTime.now();
}