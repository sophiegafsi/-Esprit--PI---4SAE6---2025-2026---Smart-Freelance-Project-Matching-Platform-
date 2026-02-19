import { Component, OnInit } from '@angular/core';
import { EvaluationService, Evaluation } from '../evaluation.service';

@Component({
  selector: 'app-evaluation-list-page',
  templateUrl: './evaluation-list-page.component.html',
  styleUrls: ['./evaluation-list-page.component.css']
})
export class EvaluationListPageComponent implements OnInit {

  evaluations: Evaluation[] = [];

  constructor(private service: EvaluationService) { }

  ngOnInit(): void {
    this.loadEvaluations();
  }

  loadEvaluations() {
    this.service.getEvaluations().subscribe({
      next: data => this.evaluations = data,
      error: err => console.error(err)
    });
  }

  deleteEvaluation(id: number) {
    if (!confirm('Voulez-vous vraiment supprimer cette évaluation ?')) return;
    this.service.deleteEvaluation(id).subscribe({
      next: () => this.loadEvaluations(),
      error: err => console.error(err)
    });
  }
}
