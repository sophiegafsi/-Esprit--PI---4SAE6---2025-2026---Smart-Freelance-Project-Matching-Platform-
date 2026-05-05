package freelink.condidature.service;

import freelink.condidature.entity.Candidature;
import freelink.condidature.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final LanguageToolService languageToolService;
    private final org.springframework.web.client.RestTemplate restTemplate;

    private static final String PROJET_SERVICE_URL = "http://localhost:8081/projet/api/projets";

    // --- FREELANCER ACTIONS ---

    @Transactional
    public Candidature apply(UUID freelancerId, Long projectId, String coverLetter,
            org.springframework.web.multipart.MultipartFile file) {
        System.out.println(">>> Applying: Freelancer=" + freelancerId + ", Project=" + projectId);

        // 1. Verify Project exists
        Map<?, ?> project;
        try {
            project = restTemplate.getForObject(PROJET_SERVICE_URL + "/getprojet/" + projectId, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Project not found: " + e.getMessage());
        }

        // 2. Check for duplicate application
        if (candidatureRepository.findByFreelancerIdAndProjectId(freelancerId, projectId).isPresent()) {
            throw new RuntimeException("You have already applied to this project.");
        }

        // 3. Process File
        String fileName = null;
        String fileType = null;
        byte[] fileData = null;

        if (file != null && !file.isEmpty()) {
            try {
                fileName = file.getOriginalFilename();
                fileType = file.getContentType();
                fileData = file.getBytes();
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to process file upload", e);
            }
        }

        // 3.5 NLP Auto-Correction of Cover Letter
        String finalCoverLetter = coverLetter;
        if (coverLetter != null && !coverLetter.trim().isEmpty()) {
            finalCoverLetter = languageToolService.autoCorrectCoverLetter(coverLetter);
        }

        // 4. Create Application
        Candidature candidature = Candidature.builder()
                .freelancerId(freelancerId)
                .projectId(projectId)
                .coverLetter(finalCoverLetter)
                .status(Candidature.Status.PENDING)
                .fileName(fileName)
                .fileType(fileType)
                .data(fileData)
                .build();

        Candidature saved = candidatureRepository.save(candidature);
        try {
            if (project != null && project.get("clientId") != null) {
                UUID clientId = UUID.fromString(project.get("clientId").toString());
                sendNotification(clientId, "A new freelancer applied to your project.", "INFO", "/project-applications/" + projectId);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse clientId for notification: " + e.getMessage());
        }
        return saved;
    }

    @Transactional
    public Candidature updateCoverLetter(UUID candidatureId, UUID freelancerId, String newLetter) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        if (!candidature.getFreelancerId().equals(freelancerId)) {
            throw new RuntimeException("Unauthorized: You do not own this application.");
        }

        if (candidature.getStatus() != Candidature.Status.PENDING) {
            throw new RuntimeException("Cannot update application after it has been reviewed.");
        }

        candidature.setCoverLetter(newLetter);
        return candidatureRepository.save(candidature);
    }

    @Transactional
    public void deleteApplication(UUID candidatureId, UUID freelancerId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        if (!candidature.getFreelancerId().equals(freelancerId)) {
            throw new RuntimeException("Unauthorized: You do not own this application.");
        }

        candidatureRepository.delete(candidature);
    }

    public List<Candidature> getMyApplications(UUID freelancerId) {
        return candidatureRepository.findByFreelancerId(freelancerId);
    }

    public List<Candidature> getAllCandidatures() {
        return candidatureRepository.findAll();
    }

    @Transactional
    public void deleteApplicationsByProject(Long projectId) {
        candidatureRepository.deleteByProjectId(projectId);
    }

    // --- CLIENT ACTIONS ---

    public List<Candidature> getProjectApplications(Long projectId, UUID clientId) {
        Map<?, ?> project = restTemplate.getForObject(PROJET_SERVICE_URL + "/getprojet/" + projectId, Map.class);
        if (project == null || !clientId.toString().equals(project.get("clientId").toString())) {
            throw new RuntimeException("Not authorized to view these applications");
        }
        return candidatureRepository.findByProjectId(projectId);
    }

    public List<Candidature> getProjectApplicationsForAdmin(Long projectId) {
        return candidatureRepository.findByProjectId(projectId);
    }

    @Transactional
    public Candidature acceptApplication(UUID candidatureId, UUID clientId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        candidature.setStatus(Candidature.Status.ACCEPTED);
        sendNotification(candidature.getFreelancerId(), "Your application was accepted! Check your contracts.", "SUCCESS", "/my-applications");
        return candidatureRepository.save(candidature);
    }

    @Transactional
    public Candidature rejectApplication(UUID candidatureId, UUID clientId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        candidature.setStatus(Candidature.Status.REJECTED);
        sendNotification(candidature.getFreelancerId(), "Your application was rejected.", "WARNING", "/my-applications");
        return candidatureRepository.save(candidature);
    }

    private void sendNotification(UUID userId, String message, String type, String actionUrl) {
        try {
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("message", message);
            payload.put("type", type);
            if (actionUrl != null) payload.put("actionUrl", actionUrl);

            restTemplate.postForObject("http://localhost:8082/api/users/" + userId + "/notifications", payload, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
