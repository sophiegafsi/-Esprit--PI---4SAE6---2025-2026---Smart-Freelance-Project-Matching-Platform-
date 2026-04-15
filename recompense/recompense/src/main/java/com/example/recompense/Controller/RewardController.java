package com.example.recompense.Controller;

import com.example.recompense.DTO.CertificatePayloadDTO;
import com.example.recompense.DTO.RewardDashboardDTO;
import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.DTO.RewardProcessingResponse;
import com.example.recompense.Entity.FreelancerRewardProfile;
import com.example.recompense.Entity.RewardHistory;
import com.example.recompense.Service.RewardEngineService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "http://localhost:4200")
public class RewardController {

    private final RewardEngineService rewardEngineService;

    public RewardController(RewardEngineService rewardEngineService) {
        this.rewardEngineService = rewardEngineService;
    }

    @PostMapping("/process-evaluation")
    public ResponseEntity<RewardProcessingResponse> processEvaluation(@RequestBody RewardEvaluationSyncRequest request) {
        return ResponseEntity.ok(rewardEngineService.processEvaluation(request));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<RewardDashboardDTO> getDashboard() {
        return ResponseEntity.ok(rewardEngineService.getDashboard());
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<FreelancerRewardProfile>> getProfiles() {
        return ResponseEntity.ok(rewardEngineService.getAllProfiles());
    }

    @GetMapping("/profiles/{email}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessEmail(authentication, #email)")
    public ResponseEntity<FreelancerRewardProfile> getProfile(@PathVariable String email) {
        return rewardEngineService.getProfile(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('admin') or (#email != null && @rewardSecurity.canAccessEmail(authentication, #email))")
    public ResponseEntity<List<RewardHistory>> getHistory(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(rewardEngineService.getHistoryForUser(email));
        }
        return ResponseEntity.ok(rewardEngineService.getAllHistory());
    }

    @GetMapping("/certificates/{historyId}")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessHistory(authentication, #historyId)")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long historyId) {
        byte[] pdf = rewardEngineService.generateCertificate(historyId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reward-certificate-" + historyId + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/certificates/{historyId}/payload")
    @PreAuthorize("hasRole('admin') or @rewardSecurity.canAccessHistory(authentication, #historyId)")
    public ResponseEntity<CertificatePayloadDTO> getCertificatePayload(@PathVariable Long historyId) {
        byte[] pdf = rewardEngineService.generateCertificate(historyId);

        return ResponseEntity.ok(new CertificatePayloadDTO(
                "reward-certificate-" + historyId + ".pdf",
                MediaType.APPLICATION_PDF_VALUE,
                Base64.getEncoder().encodeToString(pdf)
        ));
    }

    @PostMapping("/certificates/{historyId}/resend-email")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> resendCertificateEmail(@PathVariable Long historyId,
                                                         @RequestParam(required = false) String recipientEmail) {
        rewardEngineService.resendRewardEmail(historyId, recipientEmail);
        return ResponseEntity.ok("Reward email resent successfully.");
    }

    @PostMapping("/recalculate-levels")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, Object>> recalculateLevels() {
        int updatedProfiles = rewardEngineService.recalculateStoredLevels();
        return ResponseEntity.ok(Map.of(
                "message", "Stored freelancer levels recalculated successfully.",
                "updatedProfiles", updatedProfiles
        ));
    }
}
