import { Component } from '@angular/core';
import { ReviewService } from '../../../services/review.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-review',
  templateUrl: './add-review.component.html',
  styleUrls: ['./add-review.component.css']
})
export class AddReviewComponent {
  review: any = {
    evaluatorName: '',
    score: 0,
    comment: ''
  };

  constructor(private service: ReviewService, private router: Router) {}

  submit(): void {
    if (this.review.score < 1 || this.review.score > 5) {
      alert('La note doit être entre 1 et 5');
      return;
    }
    if (!this.review.comment.trim()) {
      alert('Le commentaire ne peut pas être vide');
      return;
    }
    if (!this.review.evaluatorName.trim()) {
      alert('Veuillez saisir votre nom');
      return;
    }

    this.service.addReview(this.review).subscribe({
      next: () => {
        alert('Avis ajouté avec succès');
        this.router.navigate(['/reviews']);
      },
      error: (err) => {
        console.error('Erreur ajout', err);
        alert('Erreur lors de l\'ajout. Vérifiez la console.');
      }
    });
  }
}