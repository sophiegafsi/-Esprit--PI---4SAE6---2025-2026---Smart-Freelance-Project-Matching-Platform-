package com.example.recompense.Controller;

import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.DTO.RewardProcessingResponse;
import com.example.recompense.Entity.Recompense;
import com.example.recompense.Service.RecompenseService;
import com.example.recompense.Service.RewardEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recompenses")
@CrossOrigin(origins = "http://localhost:4200")
public class RecompenseController {

    private final RecompenseService recompenseService;
    private final RewardEngineService rewardEngineService;

    public RecompenseController(RecompenseService recompenseService,
                                RewardEngineService rewardEngineService) {
        this.recompenseService = recompenseService;
        this.rewardEngineService = rewardEngineService;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Recompense> getAllRecompenses() {
        return recompenseService.getAllRecompenses();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Recompense> getRecompenseById(@PathVariable Long id) {
        return recompenseService.getRecompenseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('admin','client','freelancer')")
    public List<Recompense> getActiveRecompenses() {
        return recompenseService.getActiveRecompenses();
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Recompense> createRecompense(@RequestBody Recompense recompense) {
        return new ResponseEntity<>(recompenseService.createRecompense(recompense), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Recompense> updateRecompense(@PathVariable Long id, @RequestBody Recompense recompenseDetails) {
        return ResponseEntity.ok(recompenseService.updateRecompense(id, recompenseDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteRecompense(@PathVariable Long id) {
        recompenseService.deleteRecompense(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign-badge")
    public ResponseEntity<RewardProcessingResponse> assignBadge(@RequestBody RewardEvaluationSyncRequest request) {
        return ResponseEntity.ok(rewardEngineService.processEvaluation(request));
    }
}
