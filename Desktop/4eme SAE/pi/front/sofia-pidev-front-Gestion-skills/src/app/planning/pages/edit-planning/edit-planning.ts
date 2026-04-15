import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { PlanningService } from '../../services/planning';
import { PopupService } from '../../services/popup.service';

@Component({
  selector: 'app-edit-planning',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './edit-planning.html',
  styleUrl: './edit-planning.css'
})
export class EditPlanning implements OnInit {
  planningId!: number;

  planning: Planning = {
    id: 0,
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    status: 'ACTIVE'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private planningService: PlanningService,
    private popupService: PopupService
  ) { }

  ngOnInit(): void {
    this.planningId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPlanning();
  }

  private formatBackendError(err: any): string {
    console.log('FULL ERROR BODY:', err?.error);

    const errorBody = err?.error;

    // 1. Handle validation errors (Map<String, String> under 'messages')
    if (errorBody?.messages && typeof errorBody.messages === 'object') {
      return Object.entries(errorBody.messages)
        .map(([field, msg]) => `<strong>${field}:</strong> ${msg}`)
        .join('<br>');
    }

    // 2. Handle business errors or simple messages
    if (errorBody?.message && typeof errorBody.message === 'string') {
      return errorBody.message;
    }

    // 3. Handle Spring's default 'error' field
    if (errorBody?.error && typeof errorBody.error === 'string' && errorBody.error !== 'Validation Error' && errorBody.error !== 'Business Error') {
      return errorBody.error;
    }

    // 4. Handle cases where errorBody itself is a string
    if (typeof errorBody === 'string') {
      return errorBody;
    }

    // 5. Ultimate fallback to HTTP message
    return err?.message || 'An unexpected error occurred.';
  }

  loadPlanning(): void {
    this.planningService.getPlanningById(this.planningId).subscribe({
      next: (data: any) => {
        this.planning.id = data.id;
        this.planning.title = data.title ?? '';
        this.planning.description = data.description ?? '';
        this.planning.startDate = data.startDate ?? '';
        this.planning.endDate = data.endDate ?? '';
        this.planning.status = data.status ?? 'ACTIVE';
      },
      error: (err: any) => {
        console.error('Erreur chargement planning', err);
        this.popupService.error('Error', 'Planning not found.');
        this.router.navigate(['/planning']);
      }
    });
  }

  updatePlanning(): void {
    this.popupService.close();

    this.planningService.updatePlanning(this.planningId, this.planning).subscribe({
      next: () => {
        this.popupService.show(
          'success',
          'Success',
          'Planning updated successfully.',
          {
            confirmText: 'OK',
            onConfirm: () => {
              this.router.navigate(['/planning']);
            }
          }
        );
      },
      error: (err: any) => {
        console.error('Error updating planning', err);
        this.popupService.error('Validation Error', this.formatBackendError(err));
      }
    });
  }
}