package com.example.reclamation.Controllers;

import com.example.reclamation.dto.DuplicateCheckRequestDTO;
import com.example.reclamation.dto.DuplicateCheckResponseDTO;
import com.example.reclamation.dto.ReclamationDTO;
import com.example.reclamation.entites.Priorite;
import com.example.reclamation.entites.Reclamation;
import com.example.reclamation.entites.Statut;
import com.example.reclamation.entites.Type;
import com.example.reclamation.services.DuplicateDetectionService;
import com.example.reclamation.services.IReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ReclamationController {

    private final IReclamationService reclamationService;
    private final DuplicateDetectionService duplicateDetectionService;

    @PostMapping("/addreclamation")
    public ResponseEntity<Reclamation> createReclamation(@RequestBody Reclamation reclamation) {
        Reclamation created = reclamationService.createReclamation(reclamation);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ReclamationDTO>> getAllReclamations() {
        List<ReclamationDTO> reclamations = reclamationService.getAllReclamations();
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ReclamationDTO> getReclamationById(@PathVariable Integer id) {
        ReclamationDTO reclamation = reclamationService.getReclamationById(id);
        return ResponseEntity.ok(reclamation);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Reclamation> updateReclamation(@PathVariable Integer id,
                                                         @RequestBody Reclamation reclamation) {
        Reclamation updated = reclamationService.updateReclamation(id, reclamation);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReclamation(@PathVariable Integer id) {
        reclamationService.deleteReclamation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReclamationDTO>> searchReclamations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Type type,
            @RequestParam(required = false) Priorite priorite,
            @RequestParam(required = false) Statut statut
    ) {
        List<ReclamationDTO> reclamations = reclamationService.searchReclamations(search, type, priorite, statut);
        return ResponseEntity.ok(reclamations);
    }

    @PostMapping("/check-duplicates")
    public ResponseEntity<List<DuplicateCheckResponseDTO>> checkDuplicates(
            @RequestBody DuplicateCheckRequestDTO request
    ) {
        List<DuplicateCheckResponseDTO> duplicates =
                duplicateDetectionService.findSimilarReclamations(request);

        return ResponseEntity.ok(duplicates);
    }
}