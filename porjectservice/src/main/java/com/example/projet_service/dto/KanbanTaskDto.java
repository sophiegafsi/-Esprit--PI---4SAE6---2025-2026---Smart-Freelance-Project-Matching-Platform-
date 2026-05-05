package com.example.projet_service.dto;

import java.time.LocalDate;

public class KanbanTaskDto {
    public Long id;
    public String taskname;
    public String description;
    public LocalDate deadline;

    public Long projetId;
    public String projetTitle;

    public String status;    // todo | inprogress | urgent | overdue
    public String priorite;  // Normal | Moyenne | Haute

    public long joursRestants;
}