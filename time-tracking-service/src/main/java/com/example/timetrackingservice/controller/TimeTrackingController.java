package com.example.timetrackingservice.controller;

import com.example.timetrackingservice.dto.SnapshotRequest;
import com.example.timetrackingservice.dto.StartSessionRequest;
import com.example.timetrackingservice.entity.SessionStatus;
import com.example.timetrackingservice.entity.WorkSession;
import com.example.timetrackingservice.entity.WorkSnapshot;
import com.example.timetrackingservice.service.TimeTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @GetMapping("/{sessionId}/download-snapshots")
    public ResponseEntity<byte[]> downloadSnapshots(@PathVariable UUID sessionId) {
        List<WorkSnapshot> snapshots = timeTrackingService.getSnapshotsForSession(sessionId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            int index = 1;
            for (WorkSnapshot snapshot : snapshots) {
                String screenshotUrl = snapshot.getScreenshotUrl();
                if (screenshotUrl == null || screenshotUrl.isBlank()) continue;
                try (InputStream is = new URL(screenshotUrl).openStream()) {
                    String ext = screenshotUrl.contains(".") 
                        ? screenshotUrl.substring(screenshotUrl.lastIndexOf('.')) 
                        : ".png";
                    ZipEntry entry = new ZipEntry("screenshot_" + index + ext);
                    zos.putNextEntry(entry);
                    zos.write(is.readAllBytes());
                    zos.closeEntry();
                    index++;
                } catch (Exception e) {
                    // Skip unreadable snapshots
                }
            }
            zos.finish();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "session-" + sessionId + "-screenshots.zip");
            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
