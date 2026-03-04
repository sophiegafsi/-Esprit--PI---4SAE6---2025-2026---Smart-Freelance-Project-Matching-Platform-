package freelink.condidature.service;

import freelink.condidature.entity.Candidature;
import freelink.condidature.entity.Project;
import freelink.condidature.repository.CandidatureRepository;
import freelink.condidature.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final ProjectRepository projectRepository;

    // --- FREELANCER ACTIONS ---

    @Transactional
    public Candidature apply(UUID freelancerId, UUID projectId, String coverLetter,
            org.springframework.web.multipart.MultipartFile file) {
        System.out.println(">>> Applying: Freelancer=" + freelancerId + ", Project=" + projectId);

        // 1. Verify Project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        System.out.println(">>> Project found: " + project.getId());

        // 2. Check for duplicate application
        if (candidatureRepository.findByFreelancerIdAndProjectId(freelancerId, projectId).isPresent()) {
            throw new RuntimeException("You have already applied to this project.");
        }
        System.out.println(">>> No duplicate found.");

        // 3. Process File
        String fileName = null;
        String fileType = null;
        byte[] fileData = null;

        if (file != null && !file.isEmpty()) {
            try {
                fileName = file.getOriginalFilename();
                fileType = file.getContentType();
                fileData = file.getBytes();
                System.out.println(">>> File attached: " + fileName + " (" + file.getSize() + " bytes)");
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to process file upload", e);
            }
        }

        // 4. Create Application
        Candidature candidature = Candidature.builder()
                .freelancerId(freelancerId)
                .project(project)
                .projectId(projectId)
                .coverLetter(coverLetter)
                .status(Candidature.Status.PENDING)
                .fileName(fileName)
                .fileType(fileType)
                .data(fileData)
                .build();

        System.out.println(">>> Saving candidature...");
        Candidature saved = candidatureRepository.save(candidature);
        System.out.println(">>> Candidature saved: " + saved.getId());
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

    // --- CLIENT ACTIONS ---

    public List<Candidature> getProjectApplications(UUID projectId, UUID clientId) {
        System.out.println(">>> getProjectApplications: Project=" + projectId + ", Client=" + clientId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        System.out.println(">>> Project found: " + project.getId() + ", ClientId in DB: " + project.getClientId());

        if (project.getClientId() == null) {
            System.err.println(">>> ERROR: Project has NULL clientId!");
            throw new RuntimeException("Project data integrity error: Client ID is missing.");
        }

        if (!project.getClientId().equals(clientId)) {
            throw new RuntimeException("Unauthorized: You are not the owner of this project.");
        }

        List<Candidature> apps = candidatureRepository.findByProjectId(projectId);
        System.out.println(">>> Found " + apps.size() + " applications.");
        return apps;
    }

    @Transactional
    public Candidature acceptApplication(UUID candidatureId, UUID clientId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        Project project = projectRepository.findById(candidature.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project associated with this candidature not found"));

        if (!project.getClientId().equals(clientId)) {
            throw new RuntimeException("Unauthorized: You do not own the project for this application.");
        }

        candidature.setStatus(Candidature.Status.ACCEPTED);
        // Note: Logic to reject other applications or notify freelancer could go here.
        return candidatureRepository.save(candidature);
    }

    @Transactional
    public Candidature rejectApplication(UUID candidatureId, UUID clientId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        Project project = projectRepository.findById(candidature.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project associated with this candidature not found"));

        if (!project.getClientId().equals(clientId)) {
            throw new RuntimeException("Unauthorized: You do not own the project for this application.");
        }

        candidature.setStatus(Candidature.Status.REJECTED);
        return candidatureRepository.save(candidature);
    }

    // --- TEMPORARY HELPERS ---
    public Project createProject(Project project) {
        System.out.println(">>> Creating Project: " + project);
        if (project.getClientId() == null) {
            System.out.println(">>> WARNING: Creating project with NULL clientId!");
        }
        return projectRepository.save(project);
    }

    public Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByClient(UUID clientId) {
        return projectRepository.findByClientId(clientId);
    }
}
