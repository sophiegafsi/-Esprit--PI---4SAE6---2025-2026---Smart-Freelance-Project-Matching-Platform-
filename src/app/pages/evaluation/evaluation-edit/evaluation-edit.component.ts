import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EvaluationService, Evaluation } from '../evaluation.service';

@Component({
  selector: 'app-evaluation-edit',
  templateUrl: './evaluation-edit.component.html'
})
export class EvaluationEditComponent implements OnInit {

  model: Evaluation = {} as Evaluation;
  idE!: number;
  loading: boolean = false;

  typeEvaluationOptions = [
    'CLIENT_TO_FREELANCER',
    'FREELANCER_TO_CLIENT'
  ];

  constructor(
    private service: EvaluationService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.idE = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEvaluation();
  }

  loadEvaluation() {
    this.loading = true;
    this.service.getEvaluations().subscribe({
      next: (evaluations) => {
        const found = evaluations.find(e => e.idE === this.idE);
        if (found) this.model = found;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  save() {
    this.loading = true;
    this.service.updateEvaluation(this.model).subscribe({
      next: () => {
        alert('Évaluation mise à jour !');
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        alert('Erreur lors de la mise à jour');
      }
    });
  }
}
