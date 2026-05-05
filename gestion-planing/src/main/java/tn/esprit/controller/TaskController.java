package tn.esprit.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entities.Task;
import tn.esprit.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/search")
    public List<Task> searchTasks(@RequestParam String keyword) {
        return taskService.searchTasks(keyword);
    }

    @GetMapping("/planning/{planningId}")
    public List<Task> getTasksByPlanningId(@PathVariable Long planningId) {
        return taskService.getTasksByPlanningId(planningId);
    }

    @GetMapping("/planning/{planningId}/search")
    public List<Task> searchTasksByPlanning(@PathVariable Long planningId, @RequestParam String keyword) {
        return taskService.searchTasksByPlanning(planningId, keyword);
    }

    @PostMapping("/planning/{planningId}")
    public Task addTaskToPlanning(@PathVariable Long planningId, @Valid @RequestBody Task task) {
        return taskService.addTaskToPlanning(planningId, task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "Task deleted successfully";
    }
}