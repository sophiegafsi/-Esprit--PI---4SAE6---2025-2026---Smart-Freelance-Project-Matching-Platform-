import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjetService } from '../services/projet.service';



@Component({
  selector: 'app-add-projet-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-projet-detail.html',
  styleUrls: ['./add-projet-detail.css']
})
export class AddProjetDetailComponent implements OnInit {
  today: string = new Date().toISOString().split('T')[0];
  projetId: number | null = null;

  task = {
    taskname: '',
    description: '',
    deadline: ''
  };

  loading: boolean = false;
  error: string | null = null;
  success: string | null = null;



  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.projetId = +params['id'];
      console.log('ID du projet reçu:', this.projetId);
    });
  }

  onSubmit(): void {
    // Vérifier que tous les champs sont remplis
    if (!this.task.taskname || !this.task.description || !this.task.deadline) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    if (!this.projetId) {
      this.error = 'ID du projet non valide';
      return;
    }

    this.loading = true;
    this.error = null;
    this.success = null;

    // Préparer les données pour l'API - SANS projetId
    const taskData = {
      taskname: this.task.taskname,
      description: this.task.description,
      deadline: this.task.deadline
      // ❌ NE PAS envoyer projetId ici car il est déjà dans l'URL
    };

    console.log('Données à envoyer:', taskData);
    console.log('URL:', `${this.projetService.getApiUrl()}/api/projets/${this.projetId}/taches/addtache`);

    // Appel au service pour ajouter la tâche
    this.projetService.addTaskToProject(this.projetId, taskData).subscribe({
      next: (response) => {
        console.log('✅ Tâche ajoutée avec succès:', response);
        this.loading = false;
        this.success = 'Tâche ajoutée avec succès !';

        // Rediriger vers la liste des projets après 2 secondes
        setTimeout(() => {
          this.router.navigate(['/projet-dashboard']);
        }, 2000);
      },
      error: (err) => {
        console.error('❌ Erreur lors de l\'ajout:', err);
        this.loading = false;

        // Message d'erreur plus détaillé
        if (err.status === 404) {
          this.error = 'Projet non trouvé. Vérifiez que le projet existe encore.';
        } else if (err.status === 500) {
          this.error = 'Erreur serveur. Vérifiez les logs du backend.';
        } else if (err.status === 0) {
          this.error = 'Impossible de contacter le serveur. Vérifiez que le backend est démarré sur le port 8081.';
        } else {
          this.error = `Erreur lors de l'ajout de la tâche (${err.status}). Veuillez réessayer.`;
        }
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/projet-dashboard']);
  }
}
