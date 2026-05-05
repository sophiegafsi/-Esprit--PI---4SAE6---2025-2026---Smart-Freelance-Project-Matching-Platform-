package com.example.timetrackingservice.service;

import com.example.timetrackingservice.entity.SessionStatus;
import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.repository.WorkSessionRepository;
import com.example.timetrackingservice.repository.WorkSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeTrackingServiceTest {

    @Mock
    private WorkSessionRepository workSessionRepository;

    @Mock
    private WorkSnapshotRepository workSnapshotRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TimeTrackingService timeTrackingService;

    private UUID contractId;
    private UUID freelancerId;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        contractId = UUID.randomUUID();
        freelancerId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
    }

    @Test
    void startSession_ShouldSaveAndReturnSession() {
        WorkSession session = WorkSession.builder()
                .contractId(contractId)
                .freelancerId(freelancerId)
                .build();
        
        when(workSessionRepository.save(any(WorkSession.class))).thenReturn(session);
        
        WorkSession result = timeTrackingService.startSession(contractId, freelancerId);
        
        assertNotNull(result);
        assertEquals(contractId, result.getContractId());
        assertEquals(freelancerId, result.getFreelancerId());
        verify(workSessionRepository).save(any(WorkSession.class));
    }

    @Test
    void stopSession_ShouldUpdateStatusAndEndTime() {
        WorkSession session = WorkSession.builder()
                .id(sessionId)
                .status(SessionStatus.ACTIVE)
                .startTime(java.time.LocalDateTime.now().minusHours(1))
                .contractId(contractId)
                .build();
        
        when(workSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(workSessionRepository.save(any(WorkSession.class))).thenReturn(session);
        
        WorkSession result = timeTrackingService.stopSession(sessionId);
        
        assertEquals(SessionStatus.PENDING_APPROVAL, result.getStatus());
        assertNotNull(result.getEndTime());
        assertTrue(result.getTotalMinutesWorked() >= 60);
        verify(workSessionRepository).save(session);
    }

    @Test
    void stopSession_WhenInactive_ShouldThrowException() {
        WorkSession session = WorkSession.builder()
                .id(sessionId)
                .status(SessionStatus.APPROVED)
                .build();
        
        when(workSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        
        assertThrows(RuntimeException.class, () -> timeTrackingService.stopSession(sessionId));
    }

    @Test
    void updateSessionStatus_ShouldChangeStatus() {
        WorkSession session = WorkSession.builder()
                .id(sessionId)
                .status(SessionStatus.PENDING_APPROVAL)
                .freelancerId(freelancerId)
                .build();
        
        when(workSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(workSessionRepository.save(any(WorkSession.class))).thenReturn(session);
        
        WorkSession result = timeTrackingService.updateSessionStatus(sessionId, SessionStatus.APPROVED);
        
        assertEquals(SessionStatus.APPROVED, result.getStatus());
        verify(workSessionRepository).save(session);
    }
}
