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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIntegrationTest {

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
    void addTaskToPlanning_shouldReturnSavedTask_whenPayloadIsValid() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Task planning"));
        Task task = createTask("Implementation", "09:00", "11:00", TaskPriority.HIGH, TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks/planning/{planningId}", planning.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Implementation"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void addTaskToPlanning_shouldReturnBadRequest_whenBusinessRuleFails() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Invalid task planning"));
        Task task = createTask("Broken slot", "11:00", "10:30", TaskPriority.MEDIUM, TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks/planning/{planningId}", planning.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("End time must be after start time"));
    }

    @Test
    void addTaskToPlanning_shouldReturnBadRequest_whenValidationFails() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Validation planning"));
        String invalidPayload = """
                {
                  "title": "",
                  "description": "",
                  "taskDate": null,
                  "startTime": null,
                  "endTime": null,
                  "priority": null,
                  "status": null
                }
                """;

        mockMvc.perform(post("/api/tasks/planning/{planningId}", planning.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.messages.title").exists())
                .andExpect(jsonPath("$.messages.description").exists())
                .andExpect(jsonPath("$.messages.taskDate").exists());
    }

    @Test
    void updateTask_shouldReturnUpdatedTask_whenPayloadIsValid() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Update task planning"));
        Task savedTask = taskRepository.save(createTaskForPlanning(planning, "Analysis", "08:00", "09:00", TaskPriority.LOW, TaskStatus.TODO));

        Task update = createTask("Analysis updated", "13:00", "15:00", TaskPriority.HIGH, TaskStatus.DONE);

        mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Analysis updated"))
                .andExpect(jsonPath("$.startTime").value("13:00:00"))
                .andExpect(jsonPath("$.endTime").value("15:00:00"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void searchTasksByPlanning_shouldReturnFilteredTasks() throws Exception {
        Planning planning = planningRepository.save(createPlanning("Search planning"));
        taskRepository.save(createTaskForPlanning(planning, "API tests", "08:00", "09:00", TaskPriority.HIGH, TaskStatus.IN_PROGRESS));
        taskRepository.save(createTaskForPlanning(planning, "UI polish", "09:30", "10:00", TaskPriority.LOW, TaskStatus.TODO));

        mockMvc.perform(get("/api/tasks/planning/{planningId}/search", planning.getId())
                        .param("keyword", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("API tests"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTaskById_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 12345L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id: 12345"));
    }

    private Planning createPlanning(String title) {
        Planning planning = new Planning();
        planning.setTitle(title);
        planning.setDescription("Planning description for " + title);
        planning.setStartDate(LocalDate.now().plusDays(1));
        planning.setEndDate(LocalDate.now().plusDays(6));
        planning.setStatus(PlanningStatus.ACTIVE);
        return planning;
    }

    private Task createTask(String title, String start, String end, TaskPriority priority, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setTaskDate(LocalDate.now().plusDays(1));
        task.setStartTime(LocalTime.parse(start));
        task.setEndTime(LocalTime.parse(end));
        task.setPriority(priority);
        task.setStatus(status);
        return task;
    }

    private Task createTaskForPlanning(Planning planning, String title, String start, String end, TaskPriority priority, TaskStatus status) {
        Task task = createTask(title, start, end, priority, status);
        task.setPlanning(planning);
        return task;
    }
}
