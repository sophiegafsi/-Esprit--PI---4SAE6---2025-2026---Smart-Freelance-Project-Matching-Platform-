package com.example.projet_service.services;

import com.example.projet_service.Repositories.ProjetDetailleRepository;
import com.example.projet_service.dto.KanbanTaskDto;
import com.example.projet_service.entites.ProjetDetaille;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KanbanService {

    private final ProjetDetailleRepository projetDetailleRepository;

    public List<KanbanTaskDto> getKanbanTasks() {
        LocalDate today = LocalDate.now();

        return projetDetailleRepository.findAll().stream().map(task -> {
            KanbanTaskDto dto = new KanbanTaskDto();
            dto.id = task.getId();
            dto.taskname = task.getTaskname();
            dto.description = task.getDescription();
            dto.deadline = task.getDeadline();

            // projet info
            if (task.getProjet() != null) {
                dto.projetId = task.getProjet().getId();
                dto.projetTitle = task.getProjet().getTitle();
            }

            // si deadline null => todo par défaut
            if (dto.deadline == null) {
                dto.status = "todo";
                dto.priorite = "Normal";
                dto.joursRestants = 999;
                return dto;
            }

            long diffDays = ChronoUnit.DAYS.between(today, dto.deadline);
            dto.joursRestants = diffDays;

            // status smart
            if (diffDays < 0) dto.status = "overdue";
            else if (diffDays <= 2) dto.status = "urgent";
            else if (diffDays <= 7) dto.status = "inprogress";
            else dto.status = "todo";

            // priorité (optionnel mais joli UI)
            if (diffDays <= 2) dto.priorite = "Haute";
            else if (diffDays <= 7) dto.priorite = "Moyenne";
            else dto.priorite = "Normal";

            return dto;
        }).toList();
    }
}