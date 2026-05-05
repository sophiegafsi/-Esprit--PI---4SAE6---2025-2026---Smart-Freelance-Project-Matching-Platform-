package com.example.projet_service.Controllers;

import com.example.projet_service.dto.KanbanTaskDto;
import com.example.projet_service.services.KanbanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kanban")

@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService kanbanService;

    @GetMapping("/tasks")
    public List<KanbanTaskDto> getAllKanbanTasks() {
        return kanbanService.getKanbanTasks();
    }
}