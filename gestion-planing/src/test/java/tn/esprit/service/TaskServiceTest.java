package tn.esprit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.entities.Planning;
import tn.esprit.entities.PlanningStatus;
import tn.esprit.entities.Task;
import tn.esprit.entities.TaskPriority;
import tn.esprit.entities.TaskStatus;
import tn.esprit.exception.BusinessException;
import tn.esprit.exception.ResourceNotFoundException;
import tn.esprit.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PlanningService planningService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTaskById_shouldThrowResourceNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(90L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(90L)
        );

        assertEquals("Task not found with id: 90", exception.getMessage());
        verify(taskRepository, times(1)).findById(90L);
    }

    @Test
    void getTasksByPlanningId_shouldValidatePlanningAndReturnPlanningTasks() {
        Planning planning = createPlanning(3L);
        List<Task> tasks = List.of(createTask(1L, "Design", "08:00", "09:00", TaskPriority.HIGH, TaskStatus.TODO));

        when(planningService.getPlanningById(3L)).thenReturn(planning);
        when(taskRepository.findByPlanningId(3L)).thenReturn(tasks);

        List<Task> result = taskService.getTasksByPlanningId(3L);

        assertEquals(1, result.size());
        verify(planningService, times(1)).getPlanningById(3L);
        verify(taskRepository, times(1)).findByPlanningId(3L);
    }

    @Test
    void addTaskToPlanning_shouldAttachPlanningAndSaveTask_whenTimesAreValid() {
        Planning planning = createPlanning(4L);
        Task task = createTask(null, "Develop", "09:00", "11:00", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS);

        when(planningService.getPlanningById(4L)).thenReturn(planning);
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.addTaskToPlanning(4L, task);

        assertEquals(planning, result.getPlanning());
        verify(planningService, times(1)).getPlanningById(4L);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void addTaskToPlanning_shouldThrowBusinessException_whenEndTimeIsNotAfterStartTime() {
        Task task = createTask(null, "Broken task", "11:00", "11:00", TaskPriority.LOW, TaskStatus.TODO);
        when(planningService.getPlanningById(4L)).thenReturn(createPlanning(4L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.addTaskToPlanning(4L, task)
        );

        assertEquals("End time must be after start time", exception.getMessage());
        verify(taskRepository, never()).save(task);
    }

    @Test
    void updateTask_shouldCopyFieldsAndSaveTask_whenInputIsValid() {
        Task existingTask = createTask(8L, "Old task", "08:00", "09:00", TaskPriority.LOW, TaskStatus.TODO);
        Task update = createTask(null, "New task", "13:00", "15:00", TaskPriority.HIGH, TaskStatus.DONE);

        when(taskRepository.findById(8L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(8L, update);

        assertEquals("New task", result.getTitle());
        assertEquals("Description New task", result.getDescription());
        assertEquals(LocalTime.of(13, 0), result.getStartTime());
        assertEquals(LocalTime.of(15, 0), result.getEndTime());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(TaskStatus.DONE, result.getStatus());
        verify(taskRepository, times(1)).findById(8L);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void updateTask_shouldThrowBusinessException_whenTimeRangeIsInvalid() {
        Task existingTask = createTask(8L, "Old task", "08:00", "09:00", TaskPriority.LOW, TaskStatus.TODO);
        Task invalidUpdate = createTask(null, "Invalid task", "15:00", "14:00", TaskPriority.HIGH, TaskStatus.DONE);

        when(taskRepository.findById(8L)).thenReturn(Optional.of(existingTask));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.updateTask(8L, invalidUpdate)
        );

        assertEquals("End time must be after start time", exception.getMessage());
        verify(taskRepository, never()).save(existingTask);
    }

    @Test
    void searchTasks_shouldReturnAllTasks_whenKeywordIsBlank() {
        List<Task> allTasks = List.of(
                createTask(1L, "Alpha", "08:00", "09:00", TaskPriority.LOW, TaskStatus.TODO),
                createTask(2L, "Beta", "09:00", "10:00", TaskPriority.HIGH, TaskStatus.DONE)
        );
        when(taskRepository.findAll()).thenReturn(allTasks);

        List<Task> result = taskService.searchTasks("   ");

        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void searchTasks_shouldMergePriorityAndStatusMatchesWithoutDuplicates() {
        Task statusMatch = createTask(2L, "Backend", "09:00", "10:00", TaskPriority.LOW, TaskStatus.IN_PROGRESS);

        when(taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("progress", "progress"))
                .thenReturn(new ArrayList<>(List.of(statusMatch)));
        when(taskRepository.findAll()).thenReturn(List.of(
                createTask(1L, "High priority task", "08:00", "09:00", TaskPriority.HIGH, TaskStatus.TODO),
                statusMatch
        ));

        List<Task> result = taskService.searchTasks("progress");

        assertEquals(1, result.size());
        assertTrue(result.contains(statusMatch));
        verify(taskRepository, times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("progress", "progress");
    }

    @Test
    void searchTasksByPlanning_shouldFilterTasksByKeywordAcrossTitleDescriptionStatusAndPriority() {
        List<Task> planningTasks = List.of(
                createTask(1L, "Design API", "Define endpoints", "08:00", "09:00", TaskPriority.MEDIUM, TaskStatus.TODO),
                createTask(2L, "Testing", "Write integration tests", "10:00", "11:00", TaskPriority.HIGH, TaskStatus.IN_PROGRESS),
                createTask(3L, "Deploy", "Release to production", "12:00", "13:00", TaskPriority.LOW, TaskStatus.DONE)
        );

        when(planningService.getPlanningById(6L)).thenReturn(createPlanning(6L));
        when(taskRepository.findByPlanningId(6L)).thenReturn(planningTasks);

        List<Task> result = taskService.searchTasksByPlanning(6L, "high");

        assertEquals(1, result.size());
        assertEquals("Testing", result.get(0).getTitle());
        verify(planningService, times(1)).getPlanningById(6L);
        verify(taskRepository, times(1)).findByPlanningId(6L);
    }

    @Test
    void deleteTask_shouldDeleteExistingTask() {
        Task task = createTask(11L, "Cleanup", "16:00", "17:00", TaskPriority.LOW, TaskStatus.DONE);
        when(taskRepository.findById(11L)).thenReturn(Optional.of(task));

        taskService.deleteTask(11L);

        verify(taskRepository, times(1)).findById(11L);
        verify(taskRepository, times(1)).delete(task);
    }

    private Planning createPlanning(Long id) {
        Planning planning = new Planning();
        planning.setId(id);
        planning.setTitle("Planning " + id);
        planning.setDescription("Planning description");
        planning.setStartDate(LocalDate.of(2026, 4, 20));
        planning.setEndDate(LocalDate.of(2026, 4, 25));
        planning.setStatus(PlanningStatus.ACTIVE);
        planning.setTasks(new ArrayList<>());
        return planning;
    }

    private Task createTask(Long id, String title, String start, String end, TaskPriority priority, TaskStatus status) {
        return createTask(id, title, "Description " + title, start, end, priority, status);
    }

    private Task createTask(Long id, String title, String description, String start, String end, TaskPriority priority, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskDate(LocalDate.of(2026, 4, 20));
        task.setStartTime(LocalTime.parse(start));
        task.setEndTime(LocalTime.parse(end));
        task.setPriority(priority);
        task.setStatus(status);
        return task;
    }
}
