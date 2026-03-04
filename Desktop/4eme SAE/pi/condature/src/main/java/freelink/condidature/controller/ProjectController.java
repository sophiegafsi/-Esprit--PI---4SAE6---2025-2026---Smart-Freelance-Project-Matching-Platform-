package freelink.condidature.controller;

import freelink.condidature.entity.Project;
import freelink.condidature.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final CandidatureService candidatureService;

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        if (project.getId() == null) {
            project.setId(UUID.randomUUID());
        }
        return ResponseEntity.ok(candidatureService.createProject(project));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(candidatureService.getProject(id));
    }

    @GetMapping
    public ResponseEntity<java.util.List<Project>> getAllProjects() {
        return ResponseEntity.ok(candidatureService.getAllProjects());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<java.util.List<Project>> getClientProjects(@PathVariable("clientId") UUID clientId) {
        return ResponseEntity.ok(candidatureService.getProjectsByClient(clientId));
    }
}
