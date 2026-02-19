import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EvaluationService, Evaluation } from '../evaluation.service';

@Component({
  selector: 'app-evaluation-create',
  templateUrl: './evaluation-create.component.html',
  styleUrls: ['./evaluation-create.component.css']
})
export class EvaluationCreateComponent implements OnInit {

  evaluation: Evaluation = {} as Evaluation;
  typeEvaluation: 'CLIENT_TO_FREELANCER' | 'FREELANCER_TO_CLIENT' = 'CLIENT_TO_FREELANCER';
  questions: string[] = [];
  scores: number[] = [];

  constructor(private service: EvaluationService, private router: Router) { }

  ngOnInit(): void {
    this.setQuestions();
  }

  setQuestions() {
    if (this.typeEvaluation === 'CLIENT_TO_FREELANCER') {
      this.questions = ['Respect des délais', 'Qualité du travail', 'Communication'];
    } else {
      this.questions = ['Clarté du brief', 'Paiement respecté', 'Communication'];
    }
    this.scores = Array(this.questions.length).fill(0);
  }

  selectScore(index: number, score: number) {
    this.scores[index] = score;
  }

  submit() {
    const avgScore = Math.round(this.scores.reduce((a,b)=>a+b,0)/this.scores.length);
    this.evaluation.score = avgScore;
    this.evaluation.typeEvaluation = this.typeEvaluation;

    this.service.createEvaluation(this.evaluation).subscribe({
      next: () => this.router.navigate(['/evaluations']),
      error: err => alert('Impossible d’envoyer l’évaluation. Vérifie la console pour les détails.')
    });
  }
}
