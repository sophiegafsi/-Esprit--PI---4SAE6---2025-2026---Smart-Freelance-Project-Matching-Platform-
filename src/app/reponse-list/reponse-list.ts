import {
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import { CommonModule, DatePipe, NgFor, NgIf, NgClass } from '@angular/common';
import { finalize } from 'rxjs/operators';
import { ReponseService } from '../services/reponse.service';
import { Reponse } from '../models/reclamation.model';

@Component({
  selector: 'app-reponse-list',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor, DatePipe, NgClass],
  templateUrl: './reponse-list.html',
  styleUrls: ['./reponse-list.css']
})
export class ReponseListComponent implements OnInit, OnChanges {
  @Input() reclamationId!: number;

  reponses: Reponse[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private reponseService: ReponseService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.reclamationId) {
      this.loadReponses();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['reclamationId'] && this.reclamationId) {
      this.loadReponses();
    }
  }

  loadReponses(): void {
    if (!this.reclamationId) {
      this.reponses = [];
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.reponseService.getReponsesByReclamation(this.reclamationId)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data: any) => {
          console.log('✅ Raw responses =', data);

          if (Array.isArray(data)) {
            this.reponses = data;
          } else if (data && data.content && Array.isArray(data.content)) {
            this.reponses = data.content;
          } else if (data && data.data && Array.isArray(data.data)) {
            this.reponses = data.data;
          } else if (data && data.reponses && Array.isArray(data.reponses)) {
            this.reponses = data.reponses;
          } else if (data && typeof data === 'object') {
            const possibleArrays = Object.values(data).filter(val => Array.isArray(val));
            this.reponses = possibleArrays.length > 0 ? (possibleArrays[0] as Reponse[]) : [];
          } else {
            this.reponses = [];
          }

          console.log('📩 Responses after processing =', this.reponses);
        },
        error: (err) => {
          console.error('Error loading responses', err);
          this.errorMessage = 'Unable to load responses.';
          this.reponses = [];
        }
      });
  }

  deleteReponse(reponseId: number): void {
    this.reponseService.deleteReponse(this.reclamationId, reponseId).subscribe({
      next: () => {
        this.reponses = this.reponses.filter(rep => rep.idReponse !== reponseId);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error deleting response', err)
    });
  }

  trackByReponse(index: number, rep: Reponse): number {
    return rep.idReponse ?? index;
  }

  getUserLabel(user?: string): string {
    switch (user) {
      case 'Admin':
        return 'Admin';
      case 'Freelance':
        return 'Freelancer';
      case 'Freelancer':
        return 'Freelancer';
      default:
        return user || '';
    }
  }
}
