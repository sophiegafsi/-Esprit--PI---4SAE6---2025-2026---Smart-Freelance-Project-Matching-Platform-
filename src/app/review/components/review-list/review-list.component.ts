import { Component, OnInit } from '@angular/core';
import { Review, ReviewService } from '../../services/review.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-review-list',
  templateUrl: './review-list.component.html',
  styleUrls: ['./review-list.component.css']
})
export class ReviewListComponent implements OnInit {
  reviews: Review[] = [];

  constructor(private reviewService: ReviewService, private router: Router) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  loadReviews() {
    this.reviewService.getAllReviews().subscribe({
      next: data => this.reviews = data,
      error: err => console.error(err)
    });
  }

  deleteReview(id?: number) {
    if (id !== undefined) {
      this.reviewService.deleteReview(id).subscribe({
        next: () => this.loadReviews(),
        error: err => console.error(err)
      });
    }
  }

  goToAdd() {
    this.router.navigate(['/reviews/add/1']); // exemple evaluationId = 1
  }

  goToEdit(reviewId: number) {
    this.router.navigate([`/reviews/edit/${reviewId}`]);
  }
}
