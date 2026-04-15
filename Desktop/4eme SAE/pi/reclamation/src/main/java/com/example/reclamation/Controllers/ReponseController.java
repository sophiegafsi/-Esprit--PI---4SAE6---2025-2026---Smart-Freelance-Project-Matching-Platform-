package com.example.reclamation.Controllers;
import com.example.reclamation.entites.Reponse;
import com.example.reclamation.services.IReponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations/{reclamationId}/reponses")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ReponseController {

    private final IReponseService reponseService;

    @PostMapping("add")
    public ResponseEntity<Reponse> addReponse(@PathVariable Integer reclamationId, @RequestBody Reponse reponse) {
        Reponse created = reponseService.addReponse(reclamationId, reponse);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("list")
    public ResponseEntity<List<Reponse>> getReponsesByReclamation(@PathVariable Integer reclamationId) {
        List<Reponse> reponses = reponseService.getReponsesByReclamationId(reclamationId);
        return ResponseEntity.ok(reponses);
    }

    @PutMapping("update/{reponseId}")
    public ResponseEntity<Reponse> updateReponse(@PathVariable Integer reponseId, @RequestBody Reponse reponse) {
        Reponse updated = reponseService.updateReponse(reponseId, reponse);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("delete/{reponseId}")
    public ResponseEntity<Void> deleteReponse(@PathVariable Integer reponseId) {
        reponseService.deleteReponse(reponseId);
        return ResponseEntity.noContent().build();
    }
}
