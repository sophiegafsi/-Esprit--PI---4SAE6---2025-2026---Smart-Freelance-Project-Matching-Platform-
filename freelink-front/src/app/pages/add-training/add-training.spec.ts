import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

type TrainingStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

@Component({
  selector: 'app-add-training',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './add-training.html',
  styleUrls: ['./add-training.css'],
})
export class AddTrainingComponent {
  // ✅ Variables utilisées par ngModel dans le HTML
  titre: string = '';
  status: TrainingStatus = 'DRAFT';
  description: string = '';

  loading = false;

  constructor(private router: Router) {}

  submit() {
    // ✅ Pour l’instant template only (plus tard tu connecteras Spring Boot)
    this.loading = true;

    // simulation
    setTimeout(() => {
      this.loading = false;

      // tu peux afficher un message
      alert(
        `Training saved (template):\nTitre: ${this.titre}\nStatus: ${this.status}\nDescription: ${this.description}`
      );

      // retour à la liste
      this.router.navigate(['/trainings']);
    }, 600);
  }

  cancel() {
    this.router.navigate(['/trainings']);
  }
}
