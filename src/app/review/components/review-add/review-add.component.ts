import { Component, OnInit } from '@angular/core';
import { Review, ReviewService } from '../../services/review.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-review-add',
  templateUrl: './review-add.component.html',
  styleUrls: ['./review-add.component.css']
})
export class ReviewAddComponent implements OnInit {
  review: Review = { comment: '', score: 0, evaluationId: 0 };
  evaluationId!: number;

  // ✅ router public pour le template
  constructor(
    private reviewService: ReviewService,
    public router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Récupère l'evaluationId depuis l'URL si nécessaire
    this.evaluationId = Number(this.route.snapshot.paramMap.get('evaluationId'));
    this.review.evaluationId = this.evaluationId;
  }

  addReview() {
    this.reviewService.addReview(this.review).subscribe({
      next: () => this.router.navigate(['/reviews']),
      error: err => console.error(err)
    });
  }

  cancel() {
    this.router.navigate(['/reviews']);
  }
}
