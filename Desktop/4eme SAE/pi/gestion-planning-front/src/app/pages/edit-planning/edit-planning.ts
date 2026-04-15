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
  ) {}

  ngOnInit(): void {
    this.planningId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPlanning();
  }

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
        this.router.navigate(['/plannings']);
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
              this.router.navigate(['/plannings']);
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