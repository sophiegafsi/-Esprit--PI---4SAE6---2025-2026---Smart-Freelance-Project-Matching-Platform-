package freelink.condidature.controller;

import freelink.condidature.entity.Candidature;
import freelink.condidature.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CandidatureController {

    private final CandidatureService candidatureService;

    // --- FREELANCER ENDPOINTS ---

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidature> apply(@RequestParam("freelancerId") UUID freelancerId,
            @RequestParam("projectId") UUID projectId,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(candidatureService.apply(freelancerId, projectId, coverLetter, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Candidature> updateApplication(@PathVariable("id") UUID id,
            @RequestParam("freelancerId") UUID freelancerId,
            @RequestBody String coverLetter) {
        return ResponseEntity.ok(candidatureService.updateCoverLetter(id, freelancerId, coverLetter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable("id") UUID id,
            @RequestParam("freelancerId") UUID freelancerId) {
        candidatureService.deleteApplication(id, freelancerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<Candidature>> getMyApplications(@RequestParam("freelancerId") UUID freelancerId) {
        return ResponseEntity.ok(candidatureService.getMyApplications(freelancerId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Candidature>> getAllCandidatures() {
        return ResponseEntity.ok(candidatureService.getAllCandidatures());
    }

    // --- CLIENT ENDPOINTS ---

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Candidature>> getProjectApplications(@PathVariable("projectId") UUID projectId,
            @RequestParam("clientId") UUID clientId) {
        return ResponseEntity.ok(candidatureService.getProjectApplications(projectId, clientId));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Candidature> acceptApplication(@PathVariable("id") UUID id,
            @RequestParam("clientId") UUID clientId) {
        return ResponseEntity.ok(candidatureService.acceptApplication(id, clientId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Candidature> rejectApplication(@PathVariable("id") UUID id,
            @RequestParam("clientId") UUID clientId) {
        return ResponseEntity.ok(candidatureService.rejectApplication(id, clientId));
    }
}
