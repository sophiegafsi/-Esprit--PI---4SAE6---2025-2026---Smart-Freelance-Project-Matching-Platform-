import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { PlanningService } from '../../services/planning';

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
    private planningService: PlanningService
  ) {}

  ngOnInit(): void {
    this.planningId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPlanning();
  }

  loadPlanning(): void {
    this.planningService.getPlanningById(this.planningId).subscribe({
      next: (data: any) => {
        console.log('Planning loaded =', data);

        this.planning.id = data.id;
        this.planning.title = data.title ?? '';
        this.planning.description = data.description ?? '';
        this.planning.startDate = data.startDate ?? '';
        this.planning.endDate = data.endDate ?? '';
        this.planning.status = data.status ?? 'ACTIVE';
      },
      error: (err: any) => {
        console.error('Erreur chargement planning', err);
        alert('Planning not found');
        this.router.navigate(['/plannings']);
      }
    });
  }
updatePlanning(): void {
  const today = new Date().toISOString().split('T')[0];

  if (!this.planning.title.trim()) {
    alert('Title is required');
    return;
  }

  if (!this.planning.description.trim()) {
    alert('Description is required');
    return;
  }

  if (!this.planning.startDate) {
    alert('Start date is required');
    return;
  }

  if (!this.planning.endDate) {
    alert('End date is required');
    return;
  }

  if (this.planning.startDate < today) {
    alert('Start date must be today or in the future');
    return;
  }

  if (this.planning.endDate <= today) {
    alert('End date must be in the future');
    return;
  }

  if (this.planning.endDate < this.planning.startDate) {
    alert('End date must be after start date');
    return;
  }

  this.planningService.updatePlanning(this.planningId, this.planning).subscribe({
    next: () => {
      alert('Planning updated successfully');
      this.router.navigate(['/plannings']);
    },
    error: (err: any) => {
      console.error('Erreur update planning', err);

      if (err?.error?.message) {
        alert(err.error.message);
      } else if (err?.error?.messages) {
        alert(JSON.stringify(err.error.messages, null, 2));
      } else {
        alert('Error while updating planning');
      }
    }
  });
}
}