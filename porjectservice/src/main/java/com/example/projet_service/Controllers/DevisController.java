package com.example.projet_service.Controllers;

import com.example.projet_service.dto.DevisRequest;
import com.example.projet_service.dto.DevisResponse;
import com.example.projet_service.services.DevisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devis")

public class DevisController {

    private final DevisService devisService;

    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    // Calculer un devis basé sur un projet existant (sans modifier les entités)
    @PostMapping("/calculate")
    public ResponseEntity<DevisResponse> calculate(@RequestBody DevisRequest req) {
        DevisResponse res = devisService.calculerDevis(req.getProjetId(), req.getDeadline());
        return ResponseEntity.ok(res);
    }
}