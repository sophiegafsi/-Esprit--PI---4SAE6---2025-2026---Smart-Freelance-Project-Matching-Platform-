import { Component, OnInit } from '@angular/core';
import { EvaluationService } from '../services/evaluation.service';
import { Evaluation } from '../models/evaluation';

@Component({
  selector: 'app-historique',
  templateUrl: './historique.component.html',
  styleUrls: ['./historique.component.css']
})
export class HistoriqueComponent implements OnInit {
  evaluations: Evaluation[] = [];
  filteredEvaluations: Evaluation[] = [];
  searchTerm: string = '';
  filterNote: number | null = null;

  constructor(private evaluationService: EvaluationService) {}

  ngOnInit(): void {
    this.loadEvaluations();
  }

  loadEvaluations(): void {
    this.evaluationService.getEvaluations().subscribe({
      next: (data) => {
        // Convertir les scores en nombres
        this.evaluations = data.map(e => ({ ...e, score: Number(e.score) }));
        this.filteredEvaluations = this.evaluations;
      },
      error: (err) => console.error('Erreur chargement historique', err)
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.toLowerCase();
    let filtered = this.evaluations;

    if (term) {
      filtered = filtered.filter(e =>
        (e.evaluatorName && e.evaluatorName.toLowerCase().includes(term)) ||
        (e.projectName && e.projectName.toLowerCase().includes(term)) ||
        (e.evaluatedUserName && e.evaluatedUserName.toLowerCase().includes(term)) ||
        (e.comment && e.comment.toLowerCase().includes(term))
      );
    }

    if (this.filterNote !== null) {
      const targetScore = Number(this.filterNote);
      filtered = filtered.filter(e => e.score === targetScore);
    }

    this.filteredEvaluations = filtered;
  }

  getAverageScore(): number {
    if (!this.filteredEvaluations.length) return 0;
    const sum = this.filteredEvaluations.reduce((acc, e) => acc + e.score, 0);
    return parseFloat((sum / this.filteredEvaluations.length).toFixed(2));
  }
}