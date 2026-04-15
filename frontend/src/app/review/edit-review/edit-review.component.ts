import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../services/review.service';
import { Review } from '../models/review.model';
import { confirmDialog } from '../../shared/dialog.util';

@Component({
  selector: 'app-edit-review',
  templateUrl: './edit-review.component.html',
  styleUrls: ['./edit-review.component.css']
})
export class EditReviewComponent implements OnInit {
  reviews: Review[] = [];
  selectedId = 0;
  model: Review = this.emptyModel();
  isLoading = false;
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';

  constructor(
    private readonly reviewService: ReviewService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.reviewService.getAll().subscribe({
      next: (reviews) => {
        this.reviews = reviews || [];
        this.isLoading = false;

        const routeId = Number(this.route.snapshot.paramMap.get('id') || 0);
        const first = Number(this.reviews[0]?.id || 0);
        this.select(routeId || this.selectedId || first);
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load reviews.';
      }
    });
  }

  select(id: number): void {
    this.selectedId = Number(id || 0);
    const found = this.reviews.find((item) => Number(item.id) === this.selectedId);
    this.model = found
      ? {
          ...found,
          score: Number(found.score || 0),
          comment: found.comment || '',
          evaluatorName: found.evaluatorName || '',
          userEmail: found.userEmail || '',
          evaluation: found.evaluation?.id ? { id: Number(found.evaluation.id) } : undefined
        }
      : this.emptyModel();
  }

  save(): void {
    if (!this.selectedId || this.isSaving) return;
    if (!this.model.comment?.trim()) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Comment is required.';
      return;
    }

    this.isSaving = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    const payload: Review = {
      ...this.model,
      score: Number(this.model.score || 0),
      comment: this.model.comment.trim(),
      evaluatorName: (this.model.evaluatorName || '').trim(),
      userEmail: (this.model.userEmail || '').trim()
    };

    this.reviewService.update(this.selectedId, payload).subscribe({
      next: (updated) => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Review #${updated?.id ?? this.selectedId} updated.`;
        this.isSaving = false;
        this.load();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to update review.';
        this.isSaving = false;
      }
    });
  }

  async deleteSelected(): Promise<void> {
    if (!this.selectedId) return;

    const confirmed = await confirmDialog('Delete this review?', 'Confirm deletion');
    if (!confirmed) return;

    this.reviewService.deleteById(this.selectedId).subscribe({
      next: () => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Review #${this.selectedId} deleted.`;
        this.selectedId = 0;
        this.model = this.emptyModel();
        this.load();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to delete review.';
      }
    });
  }

  private emptyModel(): Review {
    return {
      score: 0,
      comment: '',
      evaluatorName: '',
      userEmail: ''
    };
  }
}
