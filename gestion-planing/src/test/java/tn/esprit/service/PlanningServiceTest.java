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
import tn.esprit.gestionplaning.PlanningDailyLoadResponse;
import tn.esprit.gestionplaning.PlanningEfficiencyResponse;
import tn.esprit.gestionplaning.PlanningProgressResponse;
import tn.esprit.gestionplaning.PlanningWeightedProgressResponse;
import tn.esprit.repository.PlanningRepository;

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
class PlanningServiceTest {

    @Mock
    private PlanningRepository planningRepository;

    @InjectMocks
    private PlanningService planningService;

    @Test
    void addPlanning_shouldSavePlanning_whenDatesAreValid() {
        Planning planning = createPlanning(1L, "Sprint 1", "Main sprint");
        when(planningRepository.save(planning)).thenReturn(planning);

        Planning result = planningService.addPlanning(planning);

        assertEquals("Sprint 1", result.getTitle());
        verify(planningRepository, times(1)).save(planning);
    }

    @Test
    void addPlanning_shouldThrowBusinessException_whenEndDateIsBeforeStartDate() {
        Planning planning = createPlanning(1L, "Sprint 1", "Main sprint");
        planning.setEndDate(planning.getStartDate().minusDays(1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planningService.addPlanning(planning)
        );

        assertEquals("End date cannot be before start date", exception.getMessage());
        verify(planningRepository, never()).save(planning);
    }

    @Test
    void getPlanningById_shouldThrowResourceNotFoundException_whenPlanningDoesNotExist() {
        when(planningRepository.findById(44L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> planningService.getPlanningById(44L)
        );

        assertEquals("Planning not found with id: 44", exception.getMessage());
        verify(planningRepository, times(1)).findById(44L);
    }

    @Test
    void updatePlanning_shouldCopyFieldsAndSaveUpdatedPlanning() {
        Planning existing = createPlanning(7L, "Old title", "Old description");
        Planning update = createPlanning(null, "New title", "New description");
        update.setStatus(PlanningStatus.COMPLETED);

        when(planningRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(planningRepository.save(existing)).thenReturn(existing);

        Planning result = planningService.updatePlanning(7L, update);

        assertEquals("New title", result.getTitle());
        assertEquals("New description", result.getDescription());
        assertEquals(update.getStartDate(), result.getStartDate());
        assertEquals(update.getEndDate(), result.getEndDate());
        assertEquals(PlanningStatus.COMPLETED, result.getStatus());
        verify(planningRepository, times(1)).findById(7L);
        verify(planningRepository, times(1)).save(existing);
    }

    @Test
    void searchPlannings_shouldReturnAllPlannings_whenKeywordIsBlank() {
        List<Planning> allPlannings = List.of(
                createPlanning(1L, "Alpha", "Alpha desc"),
                createPlanning(2L, "Beta", "Beta desc")
        );
        when(planningRepository.findAll()).thenReturn(allPlannings);

        List<Planning> result = planningService.searchPlannings("   ");

        assertEquals(2, result.size());
        verify(planningRepository, times(1)).findAll();
    }

    @Test
    void searchPlannings_shouldMergeStatusMatchesWithoutDuplicates() {
        Planning titleMatch = createPlanning(1L, "Active roadmap", "Contains keyword in title");
        Planning statusMatch = createPlanning(2L, "Release", "Production release");
        statusMatch.setStatus(PlanningStatus.ACTIVE);

        when(planningRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("active", "active"))
                .thenReturn(new ArrayList<>(List.of(titleMatch)));
        when(planningRepository.findAll()).thenReturn(List.of(titleMatch, statusMatch));

        List<Planning> result = planningService.searchPlannings("ACTIVE");

        assertEquals(2, result.size());
        assertTrue(result.contains(titleMatch));
        assertTrue(result.contains(statusMatch));
        verify(planningRepository, times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("active", "active");
        verify(planningRepository, times(1)).findAll();
    }

    @Test
    void getPlanningProgress_shouldCalculateTaskCountersAndAverageProgress() {
        Planning planning = createPlanning(5L, "Metrics", "Progress metrics");
        planning.setTasks(List.of(
                createTask(TaskStatus.DONE, TaskPriority.HIGH, "08:00", "09:00", "2026-04-20"),
                createTask(TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "09:00", "10:00", "2026-04-20"),
                createTask(TaskStatus.TODO, TaskPriority.LOW, "10:00", "11:00", "2026-04-20")
        ));
        when(planningRepository.findById(5L)).thenReturn(Optional.of(planning));

        PlanningProgressResponse result = planningService.getPlanningProgress(5L);

        assertEquals(3, result.getTotalTasks());
        assertEquals(1, result.getDoneTasks());
        assertEquals(1, result.getInProgressTasks());
        assertEquals(1, result.getTodoTasks());
        assertEquals(50.0, result.getProgress());
    }

    @Test
    void getPlanningWeightedProgress_shouldIgnoreInvalidTasksAndRoundResult() {
        Planning planning = createPlanning(10L, "Weighted", "Weighted progress");
        planning.setTasks(List.of(
                createTask(TaskStatus.DONE, TaskPriority.HIGH, "08:00", "10:00", "2026-04-21"),
                createTask(TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "10:00", "11:00", "2026-04-21"),
                createTask(TaskStatus.TODO, TaskPriority.LOW, "11:00", "11:00", "2026-04-21"),
                createTask(TaskStatus.DONE, null, "12:00", "13:00", "2026-04-21")
        ));
        when(planningRepository.findById(10L)).thenReturn(Optional.of(planning));

        PlanningWeightedProgressResponse result = planningService.getPlanningWeightedProgress(10L);

        assertEquals(4, result.getTotalTasks());
        assertEquals(87.5, result.getWeightedProgress());
    }

    @Test
    void getPlanningDailyLoad_shouldAggregateMinutesAndHoursByDate() {
        Planning planning = createPlanning(12L, "Load", "Daily load");
        planning.setTasks(List.of(
                createTask(TaskStatus.DONE, TaskPriority.HIGH, "08:00", "10:00", "2026-04-22"),
                createTask(TaskStatus.TODO, TaskPriority.LOW, "10:30", "11:30", "2026-04-22"),
                createTask(TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "09:00", "10:30", "2026-04-23"),
                createTask(TaskStatus.TODO, TaskPriority.LOW, "11:00", "11:00", "2026-04-23")
        ));
        when(planningRepository.findById(12L)).thenReturn(Optional.of(planning));

        List<PlanningDailyLoadResponse> result = planningService.getPlanningDailyLoad(12L);

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 4, 22), result.get(0).getDate());
        assertEquals(2, result.get(0).getTaskCount());
        assertEquals(180, result.get(0).getTotalMinutes());
        assertEquals(3.0, result.get(0).getTotalHours());
        assertEquals(LocalDate.of(2026, 4, 23), result.get(1).getDate());
        assertEquals(1, result.get(1).getTaskCount());
        assertEquals(90, result.get(1).getTotalMinutes());
        assertEquals(1.5, result.get(1).getTotalHours());
    }

    @Test
    void getPlanningEfficiency_shouldCalculateWastedTimeDensityAndEfficiencyLevel() {
        Planning planning = createPlanning(15L, "Efficiency", "Efficiency metrics");
        planning.setTasks(List.of(
                createTask(TaskStatus.DONE, TaskPriority.HIGH, "08:00", "09:00", "2026-04-24"),
                createTask(TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "09:30", "10:30", "2026-04-24"),
                createTask(TaskStatus.TODO, TaskPriority.LOW, "11:00", "12:00", "2026-04-24")
        ));
        when(planningRepository.findById(15L)).thenReturn(Optional.of(planning));

        PlanningEfficiencyResponse result = planningService.getPlanningEfficiency(15L);

        assertEquals(3, result.getTotalTasks());
        assertEquals(60, result.getWastedMinutes());
        assertEquals(60.0, result.getAverageTaskDuration());
        assertEquals(0.75, result.getTaskDensity());
        assertEquals(100.0, result.getEfficiencyScore());
        assertEquals("HIGH", result.getEfficiencyLevel());
    }

    @Test
    void deletePlanning_shouldDeleteExistingPlanning() {
        Planning planning = createPlanning(20L, "Delete me", "To delete");
        when(planningRepository.findById(20L)).thenReturn(Optional.of(planning));

        planningService.deletePlanning(20L);

        verify(planningRepository, times(1)).findById(20L);
        verify(planningRepository, times(1)).delete(planning);
    }

    private Planning createPlanning(Long id, String title, String description) {
        Planning planning = new Planning();
        planning.setId(id);
        planning.setTitle(title);
        planning.setDescription(description);
        planning.setStartDate(LocalDate.of(2026, 4, 20));
        planning.setEndDate(LocalDate.of(2026, 4, 25));
        planning.setStatus(PlanningStatus.ACTIVE);
        planning.setTasks(new ArrayList<>());
        return planning;
    }

    private Task createTask(TaskStatus status, TaskPriority priority, String startTime, String endTime, String date) {
        Task task = new Task();
        task.setTitle("Task " + startTime);
        task.setDescription("Description " + startTime);
        task.setTaskDate(LocalDate.parse(date));
        task.setStartTime(LocalTime.parse(startTime));
        task.setEndTime(LocalTime.parse(endTime));
        task.setPriority(priority);
        task.setStatus(status);
        return task;
    }
}
