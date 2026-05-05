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
  ) {}

  private formatBackendError(err: any): string {
    console.log('BACK ERROR = ', err);

    if (err?.error?.messages && typeof err.error.messages === 'object') {
      return Object.values(err.error.messages).join('<br>');
    }

    if (err?.error?.message && typeof err.error.message === 'string') {
      return err.error.message;
    }

    if (err?.error?.error && typeof err.error.error === 'string') {
      return err.error.error;
    }

    if (typeof err?.error === 'string') {
      return err.error;
    }

    if (err?.message && typeof err.message === 'string') {
      return err.message;
    }

    return 'Something went wrong.';
  }

  savePlanning(): void {
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
              this.router.navigate(['/plannings']);
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