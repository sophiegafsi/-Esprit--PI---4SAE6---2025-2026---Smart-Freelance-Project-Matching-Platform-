package tn.esprit.service;

import org.springframework.stereotype.Service;
import tn.esprit.entities.Planning;
import tn.esprit.entities.Task;
import tn.esprit.exception.BusinessException;
import tn.esprit.exception.ResourceNotFoundException;
import tn.esprit.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final PlanningService planningService;

    public TaskService(TaskRepository taskRepository, PlanningService planningService) {
        this.taskRepository = taskRepository;
        this.planningService = planningService;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public List<Task> getTasksByPlanningId(Long planningId) {
        planningService.getPlanningById(planningId);
        return taskRepository.findByPlanningId(planningId);
    }

    public Task addTaskToPlanning(Long planningId, Task task) {
        Planning planning = planningService.getPlanningById(planningId);

        validateTaskTimes(task);

        task.setPlanning(planning);
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskDetails) {
        Task task = getTaskById(id);

        validateTaskTimes(taskDetails);

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setTaskDate(taskDetails.getTaskDate());
        task.setStartTime(taskDetails.getStartTime());
        task.setEndTime(taskDetails.getEndTime());
        task.setPriority(taskDetails.getPriority());
        task.setStatus(taskDetails.getStatus());

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    public List<Task> searchTasks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return taskRepository.findAll();
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        List<Task> titleDescMatches =
                taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        lowerKeyword,
                        lowerKeyword
                );

        List<Task> all = taskRepository.findAll();

        for (Task task : all) {
            boolean matchesStatus = task.getStatus() != null &&
                    task.getStatus().name().toLowerCase().contains(lowerKeyword);

            boolean matchesPriority = task.getPriority() != null &&
                    task.getPriority().name().toLowerCase().contains(lowerKeyword);

            if (matchesStatus || matchesPriority) {
                if (!titleDescMatches.contains(task)) {
                    titleDescMatches.add(task);
                }
            }
        }

        return titleDescMatches;
    }

    public List<Task> searchTasksByPlanning(Long planningId, String keyword) {
        List<Task> tasks = getTasksByPlanningId(planningId);

        if (keyword == null || keyword.trim().isEmpty()) {
            return tasks;
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        return tasks.stream()
                .filter(task ->
                        (task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerKeyword)) ||
                                (task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerKeyword)) ||
                                (task.getStatus() != null && task.getStatus().name().toLowerCase().contains(lowerKeyword)) ||
                                (task.getPriority() != null && task.getPriority().name().toLowerCase().contains(lowerKeyword))
                )
                .toList();
    }

    private void validateTaskTimes(Task task) {
        if (task.getStartTime() != null && task.getEndTime() != null
                && !task.getEndTime().isAfter(task.getStartTime())) {
            throw new BusinessException("End time must be after start time");
        }
    }
}