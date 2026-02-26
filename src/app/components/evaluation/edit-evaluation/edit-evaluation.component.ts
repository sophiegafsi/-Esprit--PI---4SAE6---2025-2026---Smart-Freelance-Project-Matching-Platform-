import { Component, OnInit } from '@angular/core';
import { EvaluationService } from '../../../services/evaluation.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Evaluation } from '../../../models/evaluation';

@Component({
  selector: 'app-edit-evaluation',
  templateUrl: './edit-evaluation.component.html',
  styleUrls: ['./edit-evaluation.component.css']
})
export class EditEvaluationComponent implements OnInit {
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
  id!: number;

  constructor(
    private service: EvaluationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getEvaluationById(this.id).subscribe({
      next: (data) => this.evaluation = data,
      error: (err) => {
        console.error(err);
        alert('Erreur chargement');
        this.router.navigate(['/evaluations']);
      }
    });
  }

  update(): void {
    if (this.evaluation.score < 1 || this.evaluation.score > 5) {
      alert('Le score doit être entre 1 et 5');
      return;
    }
    this.service.updateEvaluation(this.id, this.evaluation).subscribe({
      next: () => {
        alert('Mise à jour effectuée');
        this.router.navigate(['/evaluations']);
      },
      error: (err) => console.error(err)
    });
  }
}