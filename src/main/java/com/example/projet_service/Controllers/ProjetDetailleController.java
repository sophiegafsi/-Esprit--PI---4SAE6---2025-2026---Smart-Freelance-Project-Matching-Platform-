package com.example.projet_service.Controllers;

import com.example.projet_service.entites.ProjetDetaille;
import com.example.projet_service.services.IProjetDetailleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projets/{projetId}/taches")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetDetailleController {

    @Autowired
    private IProjetDetailleService projetDetailleService;

    // Ajouter une tâche à un projet
    @PostMapping("/addtache")
    public ResponseEntity<ProjetDetaille> ajouterTache(
            @PathVariable Long projetId,
            @RequestBody ProjetDetaille tache) {
        try {
            ProjetDetaille nouvelleTache = projetDetailleService.ajouterTache(projetId, tache);
            return new ResponseEntity<>(nouvelleTache, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Lister toutes les tâches d'un projet
    @GetMapping("/alltaches")
    public ResponseEntity<List<ProjetDetaille>> listerTachesParProjet(@PathVariable Long projetId) {
        List<ProjetDetaille> taches = projetDetailleService.listerTachesParProjet(projetId);
        return new ResponseEntity<>(taches, HttpStatus.OK);
    }

    // Voir une tâche spécifique
    @GetMapping("/gettache/{tacheId}")
    public ResponseEntity<ProjetDetaille> voirTache(@PathVariable Long tacheId) {
        return projetDetailleService.voirTache(tacheId)
                .map(tache -> new ResponseEntity<>(tache, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Modifier une tâche
    @PutMapping("/updatetache/{tacheId}")
    public ResponseEntity<ProjetDetaille> modifierTache(
            @PathVariable Long tacheId,
            @RequestBody ProjetDetaille tache) {
        try {
            ProjetDetaille tacheModifiee = projetDetailleService.modifierTache(tacheId, tache);
            return new ResponseEntity<>(tacheModifiee, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Supprimer une tâche
    @DeleteMapping("/deletetache/{tacheId}")
    public ResponseEntity<Void> supprimerTache(@PathVariable Long tacheId) {
        try {
            projetDetailleService.supprimerTache(tacheId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
