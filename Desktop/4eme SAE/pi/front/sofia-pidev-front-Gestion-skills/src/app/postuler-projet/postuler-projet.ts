import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProjetService } from '../services/projet.service';
import { AuthService } from '../services/auth.service';
import { DevisCalculatorComponent } from '../devis-calculator/devis-calculator.component'; // 👈 NOUVEAU

@Component({
  selector: 'app-postuler-projet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DevisCalculatorComponent],
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
    domaine: '',
    budget: 0
  };

  loading: boolean = false;

  constructor(
    private projetService: ProjetService,
    private router: Router,
    private authService: AuthService
  ) { }

  onSubmit() {
    if (this.project.title && this.project.description && this.project.date && this.project.domaine) {
      this.loading = true;

      this.authService.getCurrentUser().subscribe(user => {
        if (!user) {
          alert("Vous devez être connecté pour créer un projet.");
          this.loading = false;
          return;
        }

        const payload = {
          ...this.project,
          clientId: user.id
        };

        this.projetService.addProject(payload).subscribe({
          next: (response) => {
            console.log('Projet créé avec succès', response);
            this.loading = false;

            const projetId = response.id;

            if (projetId) {
              console.log('Redirection vers les détails du projet ID:', projetId);
              this.router.navigate(['/projet', projetId, 'add-details']);
            } else {
              console.warn('ID du projet non reçu, redirection vers la liste');
              this.router.navigate(['/projet-dashboard']);
            }
          },
          error: (err) => {
            console.error('Erreur création projet', err);
            alert('Erreur lors de la création du projet');
            this.loading = false;
          }
        });
      });
    }
  }
}
