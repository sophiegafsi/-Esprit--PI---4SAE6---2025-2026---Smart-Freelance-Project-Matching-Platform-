package com.example.timetrackingservice.service;

import com.example.timetrackingservice.entity.SessionStatus;
import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.entity.WorkSnapshot;
import com.example.timetrackingservice.repository.WorkSessionRepository;
import com.example.timetrackingservice.repository.WorkSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimeTrackingJobs {

    private final WorkSessionRepository workSessionRepository;
    private final WorkSnapshotRepository workSnapshotRepository;

    // Runs every 1 minute for testing
    @Scheduled(fixedRate = 60000)
    public void ghostTracker() {
        log.info("Running Ghost Tracker Job...");
        List<WorkSession> activeSessions = workSessionRepository.findByStatus(SessionStatus.ACTIVE);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (WorkSession session : activeSessions) {
            LocalDateTime lastActivity = session.getStartTime();
            if (session.getSnapshots() != null && !session.getSnapshots().isEmpty()) {
                // Get the most recent snapshot
                WorkSnapshot latest = session.getSnapshots().get(session.getSnapshots().size() - 1);
                lastActivity = latest.getTimestamp();
            }
            
            // If no activity for 2 minutes, close it (TESTING)
            if (Duration.between(lastActivity, now).toMinutes() >= 2) {
                log.warn("Auto-closing idle work session: " + session.getId());
                session.setEndTime(lastActivity); // Close precisely at the last known activity
                session.setStatus(SessionStatus.PENDING_APPROVAL);
                session.setTotalMinutesWorked(Duration.between(session.getStartTime(), session.getEndTime()).toMinutes());
                workSessionRepository.save(session);
            }
        }
    }

    // Runs every 1 minute for testing
    @Scheduled(fixedRate = 60000)
    public void weeklyBillingJob() {
        log.info("Running End-of-Week Billing Job...");
        List<WorkSession> approvedSessions = workSessionRepository.findByStatus(SessionStatus.APPROVED);
        
        if (approvedSessions.isEmpty()) {
            log.info("No approved sessions to bill this week.");
            return;
        }

        for (WorkSession session : approvedSessions) {
            log.info("Billing session: " + session.getId() + " - Total Minutes: " + session.getTotalMinutesWorked());
            session.setStatus(SessionStatus.BILLED);
            workSessionRepository.save(session);

            Double rate = getHourlyRate(session.getContractId());
            double hours = session.getTotalMinutesWorked() / 60.0;
            double salary = hours * rate;
            
            // Ensure session represents the correct freelancer before notifying
            // (Freelancer ID is in the session)
            if (session.getFreelancerId() != null) {
                sendNotification(session.getFreelancerId(), 
                        String.format("Session BILLED! Estimated payout: %.2f hrs * $%.2f/hr = $%.2f", hours, rate, salary), 
                        "SUCCESS", "/my-applications");
            }
        }
        log.info("Successfully calculated and processed billing for " + approvedSessions.size() + " sessions.");
    }

    // Runs every 1 minute for testing
    @Scheduled(fixedRate = 60000)
    public void storageOptimizerJob() {
        log.info("Running Storage Optimizer Job...");
        // Define 'old' as older than 5 minutes for testing
        LocalDateTime thresholdDate = LocalDateTime.now().minusMinutes(5);
        
        try {
            workSnapshotRepository.deleteByTimestampBefore(thresholdDate);
            log.info("Old screenshot snapshots before " + thresholdDate + " have been successfully purged.");
        } catch (Exception e) {
            log.error("Failed to delete old snapshots", e);
        }
    }

    private void sendNotification(java.util.UUID userId, String message, String type, String actionUrl) {
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

    private Double getHourlyRate(java.util.UUID contractId) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            java.util.Map contract = restTemplate.getForObject("http://localhost:8083/api/contracts/" + contractId, java.util.Map.class);
            if (contract != null && contract.get("hourlyRate") != null) {
                return Double.valueOf(contract.get("hourlyRate").toString());
            }
        } catch (Exception e) {
            log.error("Failed to fetch hourly rate from contract " + contractId, e);
        }
        return 0.0;
    }
}
