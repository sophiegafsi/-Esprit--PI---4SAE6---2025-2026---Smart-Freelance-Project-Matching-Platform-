package com.example.timetrackingservice.controller;

import com.example.timetrackingservice.entity.SessionStatus;
import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.service.TimeTrackingService;
import com.example.timetrackingservice.dto.StartSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TimeTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeTrackingService timeTrackingService;

    @Test
    @WithMockUser
    void startSession_ShouldReturnCreated() throws Exception {
        UUID contractId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();
        StartSessionRequest request = new StartSessionRequest();
        request.setContractId(contractId);
        request.setFreelancerId(freelancerId);

        WorkSession session = WorkSession.builder()
                .contractId(contractId)
                .freelancerId(freelancerId)
                .status(SessionStatus.ACTIVE)
                .build();

        when(timeTrackingService.startSession(any(UUID.class), any(UUID.class))).thenReturn(session);

        mockMvc.perform(post("/api/time-tracking/start")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void stopSession_ShouldReturnOk() throws Exception {
        UUID sessionId = UUID.randomUUID();
        WorkSession session = WorkSession.builder()
                .id(sessionId)
                .status(SessionStatus.PENDING_APPROVAL)
                .build();

        when(timeTrackingService.stopSession(any(UUID.class))).thenReturn(session);

        mockMvc.perform(post("/api/time-tracking/" + sessionId + "/stop")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }
}
