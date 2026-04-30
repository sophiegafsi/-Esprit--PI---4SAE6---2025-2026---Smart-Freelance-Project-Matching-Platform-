package tn.esprit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.entities.Planning;
import tn.esprit.entities.PlanningStatus;
import tn.esprit.entities.Task;
import tn.esprit.entities.TaskPriority;
import tn.esprit.entities.TaskStatus;
import tn.esprit.repository.PlanningRepository;
import tn.esprit.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlanningControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlanningRepository planningRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        planningRepository.deleteAll();
    }

    @Test
    void addPlanning_shouldReturnSavedPlanning_whenPayloadIsValid() throws Exception {
        Planning planning = new Planning();
        planning.setTitle("Sprint planning");
        planning.setDescription("Organize sprint backlog");
        planning.setStartDate(LocalDate.now().plusDays(1));
        planning.setEndDate(LocalDate.now().plusDays(5));
        planning.setStatus(PlanningStatus.ACTIVE);

        mockMvc.perform(post("/api/plannings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planning)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Sprint planning"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void addPlanning_shouldReturnBadRequest_whenBeanValidationFails() throws Exception {
        String invalidPayload = """
                {
                  "title": "",
                  "description": "abc",
                  "startDate": null,
                  "endDate": null,
                  "status": null
                }
                """;

        mockMvc.perform(post("/api/plannings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.messages.title").exists())
                .andExpect(jsonPath("$.messages.description").exists())
                .andExpect(jsonPath("$.messages.startDate").exists())
                .andExpect(jsonPath("$.messages.endDate").exists())
                .andExpect(jsonPath("$.messages.status").exists());
    }

    @Test
    void addPlanning_shouldReturnBadRequest_whenBusinessRuleFails() throws Exception {
        Planning planning = new Planning();
        planning.setTitle("Invalid dates");
        planning.setDescription("End date before start date");
        planning.setStartDate(LocalDate.now().plusDays(5));
        planning.setEndDate(LocalDate.now().plusDays(2));
        planning.setStatus(PlanningStatus.ACTIVE);

        mockMvc.perform(post("/api/plannings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planning)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("End date cannot be before start date"));
    }

    @Test
    void getPlanningProgress_shouldReturnComputedMetrics() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Metrics planning"));
        Task task1 = taskRepository.save(createTask(planning, "Task 1", TaskStatus.DONE, TaskPriority.HIGH, "08:00", "09:00"));
        Task task2 = taskRepository.save(createTask(planning, "Task 2", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "09:00", "10:00"));
        Task task3 = taskRepository.save(createTask(planning, "Task 3", TaskStatus.TODO, TaskPriority.LOW, "10:00", "11:00"));
        planning.setTasks(List.of(task1, task2, task3));

        mockMvc.perform(get("/api/plannings/{id}/progress", planning.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planningId").value(planning.getId()))
                .andExpect(jsonPath("$.totalTasks").value(3))
                .andExpect(jsonPath("$.doneTasks").value(1))
                .andExpect(jsonPath("$.inProgressTasks").value(1))
                .andExpect(jsonPath("$.todoTasks").value(1))
                .andExpect(jsonPath("$.progress").value(50.0));
    }

    @Test
    void getPlanningEfficiency_shouldReturnDailyEfficiencyIndicators() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Efficiency planning"));
        Task task1 = taskRepository.save(createTask(planning, "Morning", TaskStatus.DONE, TaskPriority.HIGH, "08:00", "09:00"));
        Task task2 = taskRepository.save(createTask(planning, "Review", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "09:30", "10:30"));
        Task task3 = taskRepository.save(createTask(planning, "Wrap up", TaskStatus.TODO, TaskPriority.LOW, "11:00", "12:00"));
        planning.setTasks(List.of(task1, task2, task3));

        mockMvc.perform(get("/api/plannings/{id}/efficiency", planning.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planningId").value(planning.getId()))
                .andExpect(jsonPath("$.totalTasks").value(3))
                .andExpect(jsonPath("$.wastedMinutes").value(60))
                .andExpect(jsonPath("$.averageTaskDuration").value(60.0))
                .andExpect(jsonPath("$.taskDensity").value(0.75))
                .andExpect(jsonPath("$.efficiencyScore").value(100.0))
                .andExpect(jsonPath("$.efficiencyLevel").value("HIGH"));
    }

    @Test
    void getPlanningById_shouldReturnNotFound_whenPlanningDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/plannings/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Planning not found with id: 9999"));
    }

    private Planning createPlanning(String title) {
        Planning planning = new Planning();
        planning.setTitle(title);
        planning.setDescription("Planning description for " + title);
        planning.setStartDate(LocalDate.now().plusDays(1));
        planning.setEndDate(LocalDate.now().plusDays(5));
        planning.setStatus(PlanningStatus.ACTIVE);
        return planning;
    }

    private Task createTask(Planning planning, String title, TaskStatus status, TaskPriority priority, String start, String end) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setTaskDate(LocalDate.now().plusDays(1));
        task.setStartTime(LocalTime.parse(start));
        task.setEndTime(LocalTime.parse(end));
        task.setPriority(priority);
        task.setStatus(status);
        task.setPlanning(planning);
        return task;
    }
}
