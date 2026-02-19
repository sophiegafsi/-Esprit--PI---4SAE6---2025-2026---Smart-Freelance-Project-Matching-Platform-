import { Component, OnInit } from '@angular/core';
import { Review, ReviewService } from '../../services/review.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-review-edit',
  templateUrl: './review-edit.component.html',
  styleUrls: ['./review-edit.component.css']
})
export class ReviewEditComponent implements OnInit {
  review: Review = { comment: '', score: 0, evaluationId: 0 };
  reviewId!: number;

  // ✅ router mis en public pour pouvoir l'utiliser dans le template
  constructor(
    private reviewService: ReviewService,
    public router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.reviewId = Number(this.route.snapshot.paramMap.get('id'));
    this.reviewService.getReview(this.reviewId).subscribe({
      next: data => this.review = data,
      error: err => console.error(err)
    });
  }

  updateReview() {
    this.reviewService.updateReview(this.review).subscribe({
      next: () => this.router.navigate(['/reviews']),
      error: err => console.error(err)
    });
  }
}
