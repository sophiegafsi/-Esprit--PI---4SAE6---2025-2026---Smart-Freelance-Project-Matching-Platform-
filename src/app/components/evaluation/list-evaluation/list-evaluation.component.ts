import { Component, OnInit } from '@angular/core';
import { EvaluationService } from '../../../services/evaluation.service';
import { Evaluation } from '../../../models/evaluation';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list-evaluation',
  templateUrl: './list-evaluation.component.html',
  styleUrls: ['./list-evaluation.component.css']
})
export class ListEvaluationComponent implements OnInit {
  evaluations: Evaluation[] = [];
  filteredEvaluations: Evaluation[] = [];
  searchTerm: string = '';
  filterScore: number | null = null;

  constructor(private service: EvaluationService, private router: Router) {}

  ngOnInit(): void {
    this.loadEvaluations();
  }

  loadEvaluations(): void {
    this.service.getEvaluations().subscribe({
      next: (data) => {
        // S'assurer que les scores sont des nombres
        this.evaluations = data.map(e => ({ ...e, score: Number(e.score) }));
        this.applyFilter();
      },
      error: (err) => console.error('Erreur chargement évaluations', err)
    });
  }

  applyFilter(): void {
    console.log('Filtre appelé avec score:', this.filterScore);
    console.log('Évaluations avant filtre:', this.evaluations);

    let filtered = this.evaluations;

    // Filtre par texte
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(e =>
        (e.projectName && e.projectName.toLowerCase().includes(term)) ||
        (e.evaluatorName && e.evaluatorName.toLowerCase().includes(term)) ||
        (e.evaluatedUserName && e.evaluatedUserName.toLowerCase().includes(term)) ||
        (e.comment && e.comment.toLowerCase().includes(term))
      );
    }

    // Filtre par note (avec conversion en nombre pour sécurité)
    if (this.filterScore !== null) {
      const targetScore = Number(this.filterScore);
      filtered = filtered.filter(e => Number(e.score) === targetScore);
    }

    this.filteredEvaluations = filtered;
    console.log('Évaluations après filtre:', this.filteredEvaluations);
  }

  deleteEvaluation(id?: number): void {
    if (id === undefined) return;
    if (confirm('Supprimer cette évaluation ?')) {
      this.service.deleteEvaluation(id).subscribe({
        next: () => this.loadEvaluations(),
        error: (err) => console.error(err)
      });
    }
  }

  editEvaluation(id?: number): void {
    if (id) this.router.navigate(['/evaluations/edit', id]);
  }

  viewHistory(): void {
    this.router.navigate(['/evaluations/history']);
  }
}