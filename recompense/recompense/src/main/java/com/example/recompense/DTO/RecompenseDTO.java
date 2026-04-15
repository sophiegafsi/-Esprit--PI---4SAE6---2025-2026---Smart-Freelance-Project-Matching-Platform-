package com.example.recompense.DTO;



import lombok.Data;

@Data
public class RecompenseDTO {
    private Long id;
    private String title;
    private String description;
    private Integer pointsRequired;
    private Integer stock;
    private String imageUrl;
    private Boolean isActive;
}