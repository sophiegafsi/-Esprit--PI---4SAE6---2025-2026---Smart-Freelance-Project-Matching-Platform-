import { Component } from '@angular/core';
import { ReviewService } from '../services/review.service';
import { Review } from '../models/review.model';

@Component({
  selector: 'app-add-review',
  templateUrl: './add-review.component.html',
  styleUrls: ['./add-review.component.css']
})
export class AddReviewComponent {
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';
  predictedSentiment = '';

  model: Review = {
    score: 5,
    comment: '',
    evaluatorName: '',
    userEmail: ''
  };

  evaluationId: number | null = null;

  constructor(private readonly reviewService: ReviewService) {}

  analyzeSentiment(): void {
    if (!this.model.comment?.trim()) return;
    this.reviewService.analyzeSentiment(this.model.comment.trim()).subscribe({
      next: (response) => {
        this.predictedSentiment = response?.sentiment || '';
      },
      error: (error) => {
        console.error(error);
        this.predictedSentiment = '';
      }
    });
  }

  submit(): void {
    if (this.isSaving) return;
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
      userEmail: (this.model.userEmail || '').trim(),
      evaluation: this.evaluationId ? { id: Number(this.evaluationId) } : undefined
    };

    this.reviewService.create(payload).subscribe({
      next: (saved) => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Review #${saved?.id ?? ''} created successfully (${saved?.sentiment || 'N/A'}).`;
        this.isSaving = false;
        this.reset();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to create review.';
        this.isSaving = false;
      }
    });
  }

  reset(): void {
    this.model = {
      score: 5,
      comment: '',
      evaluatorName: '',
      userEmail: ''
    };
    this.evaluationId = null;
    this.predictedSentiment = '';
  }
}
