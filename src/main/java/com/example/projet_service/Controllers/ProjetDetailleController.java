package com.example.projet_service.Controllers;

import com.example.projet_service.entites.ProjetDetaille;
import com.example.projet_service.services.IProjetDetailleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projets/{projetId}/taches")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetDetailleController {

    @Autowired
    private IProjetDetailleService projetDetailleService;

    // Ajouter une tâche à un projet avec validation
    @PostMapping("/addtache")
    public ResponseEntity<?> ajouterTache(
            @PathVariable Long projetId,
            @RequestBody ProjetDetaille tache) {
        try {
            // Validation du nom de la tâche
            if (tache.getTaskname() == null || tache.getTaskname().trim().isEmpty()) {
                return new ResponseEntity<>("Le nom de la tâche ne peut pas être vide", HttpStatus.BAD_REQUEST);
            }

            String taskname = tache.getTaskname().trim();
            if (taskname.length() < 3) {
                return new ResponseEntity<>("Le nom de la tâche doit contenir au minimum 3 caractères",
                        HttpStatus.BAD_REQUEST);
            }

            if (!Character.isUpperCase(taskname.charAt(0))) {
                return new ResponseEntity<>("Le nom de la tâche doit commencer par une majuscule",
                        HttpStatus.BAD_REQUEST);
            }

            // Validation de la description de la tâche
            if (tache.getDescription() == null || tache.getDescription().trim().isEmpty()) {
                return new ResponseEntity<>("La description de la tâche ne peut pas être vide",
                        HttpStatus.BAD_REQUEST);
            }

            if (tache.getDescription().length() > 500) {
                return new ResponseEntity<>("La description ne peut pas dépasser 500 caractères",
                        HttpStatus.BAD_REQUEST);
            }

            // Validation du deadline
            if (tache.getDeadline() == null) {
                return new ResponseEntity<>("La date limite (deadline) est requise", HttpStatus.BAD_REQUEST);
            }

            // Le deadline doit être dans le futur
            if (tache.getDeadline().isBefore(LocalDate.now())) {
                return new ResponseEntity<>("La date limite (deadline) doit être dans le futur",
                        HttpStatus.BAD_REQUEST);
            }

            ProjetDetaille nouvelleTache = projetDetailleService.ajouterTache(projetId, tache);
            return new ResponseEntity<>(nouvelleTache, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Projet non trouvé avec l'id: " + projetId, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'ajout de la tâche: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Modifier une tâche avec validation
    @PutMapping("/updatetache/{tacheId}")
    public ResponseEntity<?> modifierTache(
            @PathVariable Long tacheId,
            @RequestBody ProjetDetaille tache) {
        try {
            // Validation du nom de la tâche
            if (tache.getTaskname() == null || tache.getTaskname().trim().isEmpty()) {
                return new ResponseEntity<>("Le nom de la tâche ne peut pas être vide", HttpStatus.BAD_REQUEST);
            }

            String taskname = tache.getTaskname().trim();
            if (taskname.length() < 3) {
                return new ResponseEntity<>("Le nom de la tâche doit contenir au minimum 3 caractères",
                        HttpStatus.BAD_REQUEST);
            }

            if (!Character.isUpperCase(taskname.charAt(0))) {
                return new ResponseEntity<>("Le nom de la tâche doit commencer par une majuscule",
                        HttpStatus.BAD_REQUEST);
            }

            // Validation de la description
            if (tache.getDescription() == null || tache.getDescription().trim().isEmpty()) {
                return new ResponseEntity<>("La description de la tâche ne peut pas être vide",
                        HttpStatus.BAD_REQUEST);
            }

            if (tache.getDescription().length() > 500) {
                return new ResponseEntity<>("La description ne peut pas dépasser 500 caractères",
                        HttpStatus.BAD_REQUEST);
            }

            // Validation du deadline
            if (tache.getDeadline() == null) {
                return new ResponseEntity<>("La date limite (deadline) est requise", HttpStatus.BAD_REQUEST);
            }

            ProjetDetaille tacheModifiee = projetDetailleService.modifierTache(tacheId, tache);
            return new ResponseEntity<>(tacheModifiee, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Tâche non trouvée avec l'id: " + tacheId, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la modification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
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