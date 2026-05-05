package tn.esprit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import tn.esprit.entities.Planning;
import tn.esprit.entities.Task;
import tn.esprit.entities.TaskPriority;
import tn.esprit.entities.TaskStatus;
import tn.esprit.gestionplaning.PlanningEfficiencyResponse;
import tn.esprit.gestionplaning.PlanningProgressResponse;
import tn.esprit.repository.PlanningRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock
    private PlanningRepository planningRepository;

    @InjectMocks
    private PlanningService planningService;

    private void mockAuth() {
        Authentication auth = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("user-123");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should return 50% progress for a planning with mixed task statuses")
    void testGetPlanningProgressMixed() {
        // Arrange
        mockAuth();
        Planning planning = new Planning();
        planning.setId(1L);
        Task t1 = new Task(); t1.setStatus(TaskStatus.DONE); // 100
        Task t2 = new Task(); t2.setStatus(TaskStatus.TODO); // 0
        planning.setTasks(Arrays.asList(t1, t2));

        when(planningRepository.findByIdAndUserId(1L, "user-123")).thenReturn(Optional.of(planning));

        // Act
        PlanningProgressResponse response = planningService.getPlanningProgress(1L);

        // Assert
        assertEquals(50.0, response.getProgress());
        assertEquals(2, response.getTotalTasks());
    }

    @Test
    @DisplayName("Should calculate efficiency score based on wasted time between tasks")
    void testGetPlanningEfficiency() {
        // Arrange
        mockAuth();
        Planning planning = new Planning();
        planning.setId(1L);
        
        LocalDate today = LocalDate.now();
        Task t1 = new Task();
        t1.setTaskDate(today);
        t1.setStartTime(LocalTime.of(9, 0));
        t1.setEndTime(LocalTime.of(10, 0));
        
        Task t2 = new Task();
        t2.setTaskDate(today);
        t2.setStartTime(LocalTime.of(10, 30)); // 30 min gap
        t2.setEndTime(LocalTime.of(11, 30));

        planning.setTasks(Arrays.asList(t1, t2));
        when(planningRepository.findByIdAndUserId(1L, "user-123")).thenReturn(Optional.of(planning));

        // Act
        PlanningEfficiencyResponse response = planningService.getPlanningEfficiency(1L);

        // Assert
        assertTrue(response.getEfficiencyScore() > 0);
        assertEquals(30, response.getWastedMinutes());
        assertEquals(2, response.getTotalTasks());
    }
}
