package tn.esprit.gestionplaning.service;

import org.springframework.stereotype.Service;
import tn.esprit.gestionplaning.entities.Planning;
import tn.esprit.gestionplaning.entities.Task;
import tn.esprit.gestionplaning.exception.BusinessException;
import tn.esprit.gestionplaning.exception.ResourceNotFoundException;
import tn.esprit.gestionplaning.repository.PlanningRepository;
import tn.esprit.gestionplaning.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final PlanningRepository planningRepository;

    public TaskService(TaskRepository taskRepository, PlanningRepository planningRepository) {
        this.taskRepository = taskRepository;
        this.planningRepository = planningRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByPlanningId(Long planningId) {
        if (!planningRepository.existsById(planningId)) {
            throw new ResourceNotFoundException("Planning not found with id: " + planningId);
        }
        return taskRepository.findByPlanningId(planningId);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public Task addTaskToPlanning(Long planningId, Task task) {
        Planning planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new ResourceNotFoundException("Planning not found with id: " + planningId));

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

    private void validateTaskTimes(Task task) {
        if (task.getStartTime() != null && task.getEndTime() != null
                && task.getEndTime().isBefore(task.getStartTime())) {
            throw new BusinessException("End time cannot be before start time");
        }
    }
}