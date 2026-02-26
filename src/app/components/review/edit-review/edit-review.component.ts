import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../../../services/review.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-edit-review',
  templateUrl: './edit-review.component.html',
  styleUrls: ['./edit-review.component.css']
})
export class EditReviewComponent implements OnInit {
  review: any = {
    evaluatorName: '',
    score: 0,
    comment: ''
  };
  id!: number;

  constructor(
    private service: ReviewService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadReview();
  }

  loadReview(): void {
    this.service.getReviewById(this.id).subscribe({
      next: (data: any) => {
        // Adaptation si les noms de propriétés diffèrent
        this.review = {
          evaluatorName: data.evaluatorName || data.nom || '',
          score: Number(data.score) || 0,
          comment: data.comment || ''
        };
      },
      error: (err: any) => {
        console.error('Erreur chargement avis', err);
        alert('Erreur lors du chargement de l\'avis. Vérifiez la console.');
        this.router.navigate(['/reviews']);
      }
    });
  }

  update(): void {
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

    this.service.updateReview(this.id, this.review).subscribe({
      next: () => {
        alert('Avis mis à jour avec succès');
        this.router.navigate(['/reviews']);
      },
      error: (err) => {
        console.error('Erreur mise à jour', err);
        alert('Erreur lors de la mise à jour. Vérifiez la console.');
      }
    });
  }
}