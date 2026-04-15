package com.example.timetrackingservice.controller;

import com.example.timetrackingservice.dto.SnapshotRequest;
import com.example.timetrackingservice.dto.StartSessionRequest;
import com.example.timetrackingservice.entity.SessionStatus;
import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.service.TimeTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-tracking")
@RequiredArgsConstructor
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    @PostMapping("/start")
    public ResponseEntity<WorkSession> startSession(@RequestBody StartSessionRequest request) {
        return ResponseEntity.ok(timeTrackingService.startSession(request.getContractId(), request.getFreelancerId()));
    }

    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<WorkSession> stopSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(timeTrackingService.stopSession(sessionId));
    }

    @PostMapping("/{sessionId}/snapshot")
    public ResponseEntity<Void> addSnapshot(@PathVariable UUID sessionId, @RequestBody SnapshotRequest request) {
        timeTrackingService.addSnapshot(sessionId, request.getScreenshotUrl());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<WorkSession>> getSessionsByContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(timeTrackingService.getSessionsByContract(contractId));
    }
    
    @PutMapping("/{sessionId}/status")
    public ResponseEntity<WorkSession> updateSessionStatus(@PathVariable UUID sessionId, @RequestParam SessionStatus status) {
        return ResponseEntity.ok(timeTrackingService.updateSessionStatus(sessionId, status));
    }
}
