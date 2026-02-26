import { Component } from '@angular/core';
import { EvaluationService } from '../../../services/evaluation.service';
import { Router } from '@angular/router';
import { Evaluation } from '../../../models/evaluation';
import { ToastrService } from 'ngx-toastr';
@Component({
  selector: 'app-add-evaluation',
  templateUrl: './add-evaluation.component.html',
  styleUrls: ['./add-evaluation.component.css']
})
export class AddEvaluationComponent {
  evaluationTypes: string[] = ['SOFT_SKILLS', 'TECHNIQUE', 'AUTRE'];

  evaluation: Evaluation = {
    projectName: '',
    evaluatorName: '',
    evaluatedUserName: '',
    score: 0,
    comment: '',
    anonymous: false,
    typeEvaluation: 'SOFT_SKILLS'
  };

  constructor(private service: EvaluationService, private router: Router ) {}

  submit(): void {
    if (this.evaluation.score < 1 || this.evaluation.score > 5) {
      alert('Le score doit être entre 1 et 5');
      return;
    }
    if (!this.evaluation.projectName || !this.evaluation.evaluatorName || !this.evaluation.evaluatedUserName) {
      alert('Veuillez remplir tous les noms');
      return;
    }

    this.service.createEvaluation(this.evaluation).subscribe({
      next: () => {
        alert('Évaluation créée avec succès');
        this.router.navigate(['/evaluations']);
      },
      error: (err) => {
        console.error('Erreur création', err);
        alert('Erreur lors de la création. Vérifiez la console.');
      }
    });
  }
}