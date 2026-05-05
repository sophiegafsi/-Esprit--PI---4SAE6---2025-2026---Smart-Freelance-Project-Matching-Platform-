import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { ReclamationService } from '../services/reclamation.service';
import { Reclamation } from '../models/reclamation.model';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reclamation-list',
  standalone: true,
  imports: [CommonModule, RouterModule, NgIf, NgFor, NgClass, DatePipe, FormsModule],
  templateUrl: './reclamation-list.html',
  styleUrls: ['./reclamation-list.css']
})
export class ReclamationListComponent implements OnInit {
  reclamations: Reclamation[] = [];
  filteredReclamations: Reclamation[] = [];
  errorMessage = '';
  isLoading: boolean = true;
  isAdmin: boolean = false;

  filters = {
    search: '',
    type: '',
    priorite: '',
    statut: ''
  };

  constructor(
    private reclamationService: ReclamationService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('ReclamationListComponent loaded ✅');

    // Subscribe to currentUser to handle role changes and wait for auth stability
    this.authService.currentUser$.subscribe(user => {
      console.log('Auth state update in ReclamationList:', user);
      if (user) {
        this.isAdmin = this.authService.isAdmin();
        this.loadReclamations(user);
      } else if (!this.authService.isLoggedIn()) {
        console.warn('User not logged in, redirecting to login...');
        this.router.navigate(['/login']);
      }
    });
  }

  loadReclamations(currentUser: any): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.reclamationService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data: Reclamation[]) => {
          console.log('✅ Complaints loaded:', data);
          const rawData = data || [];

          // Identity-based filtering for non-admins
          if (!this.isAdmin) {
            const userId = currentUser ? currentUser.id : null;
            console.log('Filtering list for user ID:', userId);

            this.reclamations = rawData.filter(rec =>
              rec.idUtilisateur === userId || rec.idCible === userId
            );
          } else {
            this.reclamations = rawData;
          }

          this.filteredReclamations = [...this.reclamations];
        },
        error: (err) => {
          console.error('❌ Error loading complaints', err);
          this.errorMessage = 'Unable to load complaints.';
          this.reclamations = [];
          this.filteredReclamations = [];
        }
      });
  }

  applyFilters(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.reclamationService.search(this.filters)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data: Reclamation[]) => {
          console.log('✅ Filtered complaints:', data);
          const rawData = data || [];

          // Apply role filtering to search results too
          if (!this.isAdmin) {
            const currentUser = this.authService.getCurrentUserValue();
            const userId = currentUser ? currentUser.id : null;
            this.filteredReclamations = rawData.filter(rec =>
              rec.idUtilisateur === userId || rec.idCible === userId
            );
          } else {
            this.filteredReclamations = rawData;
          }
        },
        error: (err) => {
          console.error('❌ Error filtering complaints', err);
          this.errorMessage = 'Unable to filter complaints.';
          this.filteredReclamations = [];
        }
      });
  }

  resetFilters(): void {
    this.filters = {
      search: '',
      type: '',
      priorite: '',
      statut: ''
    };

    const user = this.authService.getCurrentUserValue();
    if (user) {
      this.loadReclamations(user);
    }
  }

  get totalReclamations(): number {
    return this.filteredReclamations.length;
  }

  get enAttenteCount(): number {
    return this.filteredReclamations.filter(rec => rec.statut === 'EN_ATTENTE').length;
  }

  get enCoursCount(): number {
    return this.filteredReclamations.filter(rec => rec.statut === 'EN_COURS').length;
  }

  viewDetails(id: number): void {
    this.router.navigate(['/reclamations', id]);
  }

  editReclamation(id: number): void {
    this.router.navigate(['/reclamations/edit', id]);
  }

  deleteReclamation(id: number): void {
    this.reclamationService.delete(id).subscribe({
      next: () => {
        this.reclamations = this.reclamations.filter(r => r.idReclamation !== id);
        this.filteredReclamations = this.filteredReclamations.filter(r => r.idReclamation !== id);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Deletion failed', err);
      }
    });
  }

  goToCreate(): void {
    this.router.navigate(['/reclamations/new']);
  }

  trackByReclamation(index: number, rec: Reclamation): number {
    return rec.idReclamation ?? index;
  }

  getTypeLabel(type?: string): string {
    switch (type) {
      case 'PROJET': return 'Project';
      case 'PAIEMENT': return 'Payment';
      case 'UTILISATEUR': return 'User';
      case 'TECHNIQUE': return 'Technical';
      default: return type || '';
    }
  }

  getPrioriteLabel(priorite?: string): string {
    switch (priorite) {
      case 'BASSE': return 'Low';
      case 'MOYENNE': return 'Medium';
      case 'HAUTE': return 'High';
      case 'CRITIQUE': return 'Critical';
      default: return priorite || '';
    }
  }

  getStatutLabel(statut?: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'Pending';
      case 'EN_COURS': return 'In Progress';
      case 'RESOLUE': return 'Resolved';
      case 'REJETEE': return 'Rejected';
      default: return statut || '';
    }
  }

  getUrgentReasonLabel(reason?: string): string {
    switch (reason) {
      case 'Priorité critique': return 'Critical priority';
      case 'Priorité élevée': return 'High priority';
      default: return reason || '';
    }
  }
}
