import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../../../services/review.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list-review',
  templateUrl: './list-review.component.html',
  styleUrls: ['./list-review.component.css']
})
export class ListReviewComponent implements OnInit {
  reviews: any[] = [];
  filteredReviews: any[] = [];
  searchTerm: string = '';
  filterScore: number | null = null;

  constructor(private service: ReviewService, private router: Router) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  loadReviews(): void {
    this.service.getAllReviews().subscribe({
      next: (data) => {
        // Convertir les scores en nombres et gérer les undefined
        this.reviews = data.map((r: any) => ({
          ...r,
          score: r.score !== undefined ? Number(r.score) : null
        }));
        this.applyFilter();
      },
      error: (err) => console.error('Erreur chargement avis', err)
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.toLowerCase();
    let filtered = this.reviews;

    if (term) {
      filtered = filtered.filter(r =>
        (r.evaluatorName && r.evaluatorName.toLowerCase().includes(term)) ||
        (r.comment && r.comment.toLowerCase().includes(term))
      );
    }

    if (this.filterScore !== null) {
      const targetScore = Number(this.filterScore);
      filtered = filtered.filter(r => r.score !== null && Number(r.score) === targetScore);
    }

    this.filteredReviews = filtered;
  }

  deleteReview(id?: number): void {
    if (id === undefined) return;
    if (confirm('Supprimer cet avis ?')) {
      this.service.deleteReview(id).subscribe({
        next: () => this.loadReviews(),
        error: (err) => console.error(err)
      });
    }
  }

  editReview(id?: number): void {
    if (id) this.router.navigate(['/reviews/edit', id]);
  }
}