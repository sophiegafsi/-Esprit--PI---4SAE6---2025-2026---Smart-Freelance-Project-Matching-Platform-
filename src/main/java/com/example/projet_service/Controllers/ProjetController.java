package com.example.projet_service.Controllers;

import com.example.projet_service.entites.Projet;
import com.example.projet_service.services.IProjetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projets")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetController {

    @Autowired
    private IProjetService projetService;


    // Ajouter un projet
    @PostMapping("/addprojet")
    public ResponseEntity<Projet> ajouterProjet(@RequestBody Projet projet) {
        Projet nouveauProjet = projetService.ajouterProjet(projet);
        return new ResponseEntity<>(nouveauProjet, HttpStatus.CREATED);
    }

    // Lister tous les projets
    @GetMapping("/allprojets")
    public ResponseEntity<List<Projet>> listerTousProjets() {
        List<Projet> projets = projetService.listerTousProjets();
        return new ResponseEntity<>(projets, HttpStatus.OK);
    }

    // Voir les détails d'un projet
    @GetMapping("/getprojet/{id}")
    public ResponseEntity<Projet> voirProjet(@PathVariable Long id) {
        return projetService.voirProjet(id)
                .map(projet -> new ResponseEntity<>(projet, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Modifier un projet
    @PutMapping("/updateprojet/{id}")
    public ResponseEntity<Projet> modifierProjet(@PathVariable Long id, @RequestBody Projet projet) {
        try {
            Projet projetModifie = projetService.modifierProjet(id, projet);
            return new ResponseEntity<>(projetModifie, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Supprimer un projet
    @DeleteMapping("/deleteprojet/{id}")
    public ResponseEntity<Void> supprimerProjet(@PathVariable Long id) {
        try {
            projetService.supprimerProjet(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
