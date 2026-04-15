import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../services/review.service';
import { Review } from '../models/review.model';
import { confirmDialog } from '../../shared/dialog.util';

@Component({
  selector: 'app-list-review',
  templateUrl: './list-review.component.html',
  styleUrls: ['./list-review.component.css']
})
export class ListReviewComponent implements OnInit {
  reviews: Review[] = [];
  stats: Record<string, number> = {};
  searchTerm = '';
  isLoading = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';

  constructor(private readonly reviewService: ReviewService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.isLoading = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.reviewService.getAll().subscribe({
      next: (reviews) => {
        this.reviews = reviews || [];
        this.loadStats();
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load reviews.';
      }
    });
  }

  private loadStats(): void {
    this.reviewService.getSentimentStats().subscribe({
      next: (stats) => {
        this.stats = stats || {};
        this.isLoading = false;
      },
      error: (error) => {
        console.error(error);
        this.stats = {};
        this.isLoading = false;
      }
    });
  }

  get filteredReviews(): Review[] {
    const query = this.searchTerm.trim().toLowerCase();
    return this.reviews.filter((item) => {
      if (!query) return true;
      const text = `${item.userEmail || ''} ${item.evaluatorName || ''} ${item.comment || ''} ${item.sentiment || ''}`.toLowerCase();
      return text.includes(query);
    });
  }

  get sentimentEntries(): Array<{ key: string; value: number }> {
    return Object.entries(this.stats || {}).map(([key, value]) => ({ key, value }));
  }

  async deleteReview(review: Review): Promise<void> {
    const id = Number(review?.id || 0);
    if (!id) return;

    const confirmed = await confirmDialog(`Delete review #${id}?`, 'Confirm deletion');
    if (!confirmed) return;

    this.reviewService.deleteById(id).subscribe({
      next: () => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Review #${id} deleted.`;
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
