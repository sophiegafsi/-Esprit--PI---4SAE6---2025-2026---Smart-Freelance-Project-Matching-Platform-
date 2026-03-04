import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProjetService } from '../services/projet.service';
import { DevisCalculatorComponent } from '../devis-calculator/devis-calculator.component'; // 👈 NOUVEAU


@Component({
  selector: 'app-postuler-projet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink,    DevisCalculatorComponent // 👈 NOUVEAU
  ],
  templateUrl: './postuler-projet.html',
  styleUrls: ['./postuler-projet.css'],

})
export class PostulerProjetComponent {
  today: string = new Date().toISOString().split('T')[0];
  domains = [
    'WEB', 'MOBILE', 'DESKTOP', 'DATA_SCIENCE', 'IA',
    'DEVOPS', 'CYBERSECURITY', 'CLOUD_COMPUTING', 'GAME_DEV',
    'IOT', 'BIG_DATA', 'BLOCKCHAIN'
  ];

  project = {
    title: '',
    description: '',
    date: '',
    domaine: ''
  };

  loading: boolean = false;

  constructor(
    private projetService: ProjetService,
    private router: Router
  ) {}

  onSubmit() {
    if (this.project.title && this.project.description && this.project.date && this.project.domaine) {
      this.loading = true;

      this.projetService.addProject(this.project).subscribe({
        next: (response) => {
          console.log('Projet créé avec succès', response);
          this.loading = false;

          // ✅ RÉCUPÉRER L'ID DU PROJET CRÉÉ
          const projetId = response.id; // L'API retourne le projet avec son ID

          if (projetId) {
            // ✅ REDIRIGER VERS LA PAGE D'AJOUT DES DÉTAILS
            console.log('Redirection vers les détails du projet ID:', projetId);
            this.router.navigate(['/projet', projetId, 'add-details']);
          } else {
            // Fallback: rediriger vers la liste si pas d'ID
            console.warn('ID du projet non reçu, redirection vers la liste');
            this.router.navigate(['/projets']);
          }
        },
        error: (err) => {
          console.error('Erreur création projet', err);
          alert('Erreur lors de la création du projet');
          this.loading = false;
        }
      });
    }
  }
}
