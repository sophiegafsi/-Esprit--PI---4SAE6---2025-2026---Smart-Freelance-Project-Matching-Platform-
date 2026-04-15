import { Component, OnInit } from '@angular/core';
import { EvaluationService } from '../services/evaluation.service';
import { Evaluation } from '../models/evaluation.model';
import { confirmDialog } from '../../shared/dialog.util';

@Component({
  selector: 'app-list-evaluation',
  templateUrl: './list-evaluation.component.html',
  styleUrls: ['./list-evaluation.component.css']
})
export class ListEvaluationComponent implements OnInit {
  evaluations: Evaluation[] = [];
  searchTerm = '';
  minScore = 0;
  isLoading = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';

  constructor(private readonly evaluationService: EvaluationService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.isLoading = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.evaluationService.getAll().subscribe({
      next: (evaluations) => {
        this.evaluations = evaluations || [];
        this.isLoading = false;
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load evaluations.';
      }
    });
  }

  get filteredEvaluations(): Evaluation[] {
    const query = this.searchTerm.trim().toLowerCase();
    const min = Number(this.minScore || 0);
    return this.evaluations
      .filter((item) => Number(item.score || 0) >= min)
      .filter((item) => {
        if (!query) return true;
        const text = `${item.projectName || ''} ${item.userEmail || ''} ${item.evaluatedUserEmail || ''} ${item.evaluatorName || ''}`.toLowerCase();
        return text.includes(query);
      });
  }

  get averageScore(): string {
    if (!this.evaluations.length) return '0.0';
    const total = this.evaluations.reduce((sum, item) => sum + Number(item.score || 0), 0);
    return (total / this.evaluations.length).toFixed(1);
  }

  async deleteEvaluation(item: Evaluation): Promise<void> {
    const id = Number(item?.id || 0);
    if (!id) return;

    const confirmed = await confirmDialog(`Delete evaluation #${id}?`, 'Confirm deletion');
    if (!confirmed) return;

    this.evaluationService.deleteById(id).subscribe({
      next: () => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Evaluation #${id} deleted.`;
        this.refresh();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Delete action failed.';
      }
    });
  }
}
