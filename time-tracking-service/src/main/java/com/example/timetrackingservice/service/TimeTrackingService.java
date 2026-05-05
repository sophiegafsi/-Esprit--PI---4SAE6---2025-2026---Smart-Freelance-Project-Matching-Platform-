package com.example.timetrackingservice.service;

import com.example.timetrackingservice.entity.SessionStatus;
import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.entity.WorkSnapshot;
import com.example.timetrackingservice.repository.WorkSessionRepository;
import com.example.timetrackingservice.repository.WorkSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrackingService {

    private final WorkSessionRepository workSessionRepository;
    private final WorkSnapshotRepository workSnapshotRepository;

    @Transactional
    public WorkSession startSession(UUID contractId, UUID freelancerId) {
        WorkSession session = WorkSession.builder()
                .contractId(contractId)
                .freelancerId(freelancerId)
                .build();
        WorkSession saved = workSessionRepository.save(session);

        UUID clientId = getClientId(contractId);
        if (clientId != null) sendNotification(clientId, "Freelancer started the tracker for your project.", "INFO", "/work-review/" + contractId);

        return saved;
    }

    @Transactional
    public WorkSession stopSession(UUID sessionId) {
        WorkSession session = workSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Session is not active");
        }

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.PENDING_APPROVAL);

        Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
        session.setTotalMinutesWorked(duration.toMinutes());

        WorkSession saved = workSessionRepository.save(session);

        UUID clientId = getClientId(session.getContractId());
        if (clientId != null) sendNotification(clientId, "Freelancer stopped the tracker. Please review the session.", "INFO", "/work-review/" + session.getContractId());

        return saved;
    }

    @Transactional
    public void addSnapshot(UUID sessionId, String screenshotUrl) {
        WorkSession session = workSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Cannot add snapshot to inactive session");
        }

        WorkSnapshot snapshot = WorkSnapshot.builder()
                .workSession(session)
                .screenshotUrl(screenshotUrl)
                .build();

        workSnapshotRepository.save(snapshot);
    }

    public List<WorkSession> getSessionsByContract(UUID contractId) {
        return workSessionRepository.findByContractId(contractId);
    }

    public List<WorkSnapshot> getSnapshotsForSession(UUID sessionId) {
        WorkSession session = workSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return workSnapshotRepository.findByWorkSession(session);
    }

    @Transactional
    public WorkSession updateSessionStatus(UUID sessionId, SessionStatus newStatus) {
        WorkSession session = workSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (newStatus == SessionStatus.APPROVED && session.getStatus() != SessionStatus.APPROVED) {
            sendNotification(session.getFreelancerId(), "Your work session was APPROVED by the client!", "SUCCESS", "/my-applications");
        } else if (newStatus == SessionStatus.REJECTED && session.getStatus() != SessionStatus.REJECTED) {
            sendNotification(session.getFreelancerId(), "Your work session was REJECTED by the client.", "WARNING", "/my-applications");
        }
        
        session.setStatus(newStatus);
        return workSessionRepository.save(session);
    }

    private void sendNotification(UUID userId, String message, String type, String actionUrl) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("message", message);
            payload.put("type", type);
            if (actionUrl != null) payload.put("actionUrl", actionUrl);

            restTemplate.postForObject("http://localhost:8082/api/users/" + userId + "/notifications", payload, String.class);
        } catch (Exception e) {
            log.error("Failed to send notification to user " + userId, e);
        }
    }

    private UUID getClientId(UUID contractId) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            java.util.Map contract = restTemplate.getForObject("http://localhost:8083/api/contracts/" + contractId, java.util.Map.class);
            if (contract != null && contract.get("clientId") != null) {
                return UUID.fromString(contract.get("clientId").toString());
            }
        } catch (Exception e) {
            log.error("Failed to fetch client ID from contract " + contractId, e);
        }
        return null;
    }
}
