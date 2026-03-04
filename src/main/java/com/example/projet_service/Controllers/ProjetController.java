package com.example.projet_service.Controllers;

import com.example.projet_service.entites.Projet;
import com.example.projet_service.services.IProjetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projets")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetController {

    @Autowired
    private IProjetService projetService;

    // Ajouter un projet avec validation
    @PostMapping("/addprojet")
    public ResponseEntity<?> ajouterProjet(@RequestBody Projet projet) {
        try {
            // Validation du titre
            if (projet.getTitle() == null || projet.getTitle().trim().isEmpty()) {
                return new ResponseEntity<>("Le titre ne peut pas être vide", HttpStatus.BAD_REQUEST);
            }

            String title = projet.getTitle().trim();
            if (title.length() < 4) {
                return new ResponseEntity<>("Le titre doit contenir au minimum 4 caractères", HttpStatus.BAD_REQUEST);
            }

            // Vérifier que la première lettre est en majuscule
            char premiereLettre = title.charAt(0);
            if (!Character.isUpperCase(premiereLettre)) {
                return new ResponseEntity<>("Le titre doit commencer par une majuscule", HttpStatus.BAD_REQUEST);
            }

            // Validation de la description (optionnelle mais avec longueur max)
            if (projet.getDescription() != null && projet.getDescription().length() > 1000) {
                return new ResponseEntity<>("La description ne peut pas dépasser 1000 caractères", HttpStatus.BAD_REQUEST);
            }

            // Validation de la date
            if (projet.getDate() == null) {
                return new ResponseEntity<>("La date est requise", HttpStatus.BAD_REQUEST);
            }

            // La date ne peut pas être dans le passé (optionnel)
            if (projet.getDate().isBefore(LocalDate.now())) {
                return new ResponseEntity<>("La date ne peut pas être dans le passé", HttpStatus.BAD_REQUEST);
            }

            // Validation du domaine
            if (projet.getDomaine() == null) {
                return new ResponseEntity<>("Le domaine est requis", HttpStatus.BAD_REQUEST);
            }

            Projet nouveauProjet = projetService.ajouterProjet(projet);
            return new ResponseEntity<>(nouveauProjet, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'ajout du projet: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Modifier un projet avec validation
    @PutMapping("/updateprojet/{id}")
    public ResponseEntity<?> modifierProjet(@PathVariable Long id, @RequestBody Projet projet) {
        try {
            // Validation du titre
            if (projet.getTitle() == null || projet.getTitle().trim().isEmpty()) {
                return new ResponseEntity<>("Le titre ne peut pas être vide", HttpStatus.BAD_REQUEST);
            }

            String title = projet.getTitle().trim();
            if (title.length() < 4) {
                return new ResponseEntity<>("Le titre doit contenir au minimum 4 caractères", HttpStatus.BAD_REQUEST);
            }

            if (!Character.isUpperCase(title.charAt(0))) {
                return new ResponseEntity<>("Le titre doit commencer par une majuscule", HttpStatus.BAD_REQUEST);
            }

            // Validation de la description
            if (projet.getDescription() != null && projet.getDescription().length() > 1000) {
                return new ResponseEntity<>("La description ne peut pas dépasser 1000 caractères", HttpStatus.BAD_REQUEST);
            }

            // Validation de la date
            if (projet.getDate() == null) {
                return new ResponseEntity<>("La date est requise", HttpStatus.BAD_REQUEST);
            }

            // Validation du domaine
            if (projet.getDomaine() == null) {
                return new ResponseEntity<>("Le domaine est requis", HttpStatus.BAD_REQUEST);
            }

            Projet projetModifie = projetService.modifierProjet(id, projet);
            return new ResponseEntity<>(projetModifie, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Projet non trouvé avec l'id: " + id, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la modification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
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