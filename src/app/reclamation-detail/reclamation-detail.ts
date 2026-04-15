import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { ReclamationService } from '../services/reclamation.service';
import { Reclamation } from '../models/reclamation.model';
import { ReponseListComponent } from '../reponse-list/reponse-list';
import { ReponseFormComponent } from '../reponse-form/reponse-form';

@Component({
  selector: 'app-reclamation-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NgIf,
    DatePipe,
    ReponseListComponent,
    ReponseFormComponent
  ],
  templateUrl: './reclamation-detail.html',
  styleUrls: ['./reclamation-detail.css']
})
export class ReclamationDetailComponent implements OnInit, OnDestroy {
  reclamation?: Reclamation;
  reclamationId!: number;
  isLoading = true;
  errorMessage = '';

  private destroy$ = new Subject<void>();

  @ViewChild(ReponseListComponent) reponseListComponent?: ReponseListComponent;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reclamationService: ReclamationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const idParam = params.get('id');
        console.log('route id =', idParam);

        if (!idParam) {
          this.errorMessage = 'Complaint ID not found.';
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }

        this.reclamationId = Number(idParam);

        if (isNaN(this.reclamationId)) {
          this.errorMessage = 'Invalid complaint ID.';
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }

        this.loadReclamation(this.reclamationId);
      });
  }

  loadReclamation(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.reclamation = undefined;

    this.reclamationService.getById(id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data) => {
          console.log('complaint detail =', data);
          this.reclamation = data;
        },
        error: (err) => {
          console.error('Error loading complaint detail', err);
          this.errorMessage = 'Unable to load this complaint.';
        }
      });
  }

  onReponseAdded(): void {
    this.reponseListComponent?.loadReponses();
  }

  back(): void {
    this.router.navigate(['/reclamations']);
  }

  edit(): void {
    if (this.reclamation?.idReclamation) {
      this.router.navigate(['/reclamations/edit', this.reclamation.idReclamation]);
    }
  }

  getTypeLabel(type?: string): string {
    switch (type) {
      case 'PROJET':
        return 'Project';
      case 'PAIEMENT':
        return 'Payment';
      case 'UTILISATEUR':
        return 'User';
      case 'TECHNIQUE':
        return 'Technical';
      default:
        return type || '';
    }
  }

  getPrioriteLabel(priorite?: string): string {
    switch (priorite) {
      case 'BASSE':
        return 'Low';
      case 'MOYENNE':
        return 'Medium';
      case 'HAUTE':
        return 'High';
      case 'CRITIQUE':
        return 'Critical';
      default:
        return priorite || '';
    }
  }

  getStatutLabel(statut?: string): string {
    switch (statut) {
      case 'EN_ATTENTE':
        return 'Pending';
      case 'EN_COURS':
        return 'In Progress';
      case 'RESOLUE':
        return 'Resolved';
      case 'REJETEE':
        return 'Rejected';
      default:
        return statut || '';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
