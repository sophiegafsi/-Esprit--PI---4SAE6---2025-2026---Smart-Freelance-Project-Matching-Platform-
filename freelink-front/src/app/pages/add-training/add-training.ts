import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormationService, Formation } from '../../services/formation.service';

@Component({
  selector: 'app-add-training',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-training.html'
})
export class AddTrainingComponent {
  titre = '';
  description = '';
  message = '';
  loading = false;

  constructor(private formationService: FormationService) {}

  submit(): void {
    this.loading = true;
    this.message = '';

    const payload: Formation = {
      titre: this.titre,
      description: this.description
    };

    this.formationService.create(payload).subscribe({
      next: () => {
        this.message = 'Formation ajoutée ✅';
        this.titre = '';
        this.description = '';
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error(err);
        this.message = "Erreur lors de l'ajout ❌";
        this.loading = false;
      }
    });
  }
}
