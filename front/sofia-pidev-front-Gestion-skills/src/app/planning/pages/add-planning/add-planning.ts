import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { PlanningService } from '../../services/planning';
import { PopupService } from '../../services/popup.service';

@Component({
  selector: 'app-add-planning',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './add-planning.html',
  styleUrl: './add-planning.css'
})
export class AddPlanning {
  planning: Planning = {
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    status: 'ACTIVE'
  };

  constructor(
    private planningService: PlanningService,
    private router: Router,
    private popupService: PopupService
  ) { }

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

  savePlanning(): void {
    console.log('SENDING PLANNING:', this.planning);
    this.popupService.close();

    this.planningService.addPlanning(this.planning).subscribe({
      next: () => {
        this.popupService.show(
          'success',
          'Success',
          'Planning added successfully.',
          {
            confirmText: 'OK',
            onConfirm: () => {
              this.router.navigate(['/planning']);
            }
          }
        );
      },
      error: (err: any) => {
        console.error('Error adding planning', err);
        this.popupService.error('Validation Error', this.formatBackendError(err));
      }
    });
  }
}