import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { PlanningService } from '../../services/planning';

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
    private router: Router
  ) {}

 savePlanning(): void {
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

  if (this.planning.endDate < this.planning.startDate) {
    alert('End date must be after start date');
    return;
  }

  this.planningService.addPlanning(this.planning).subscribe({
    next: () => {
      alert('Planning added successfully');
      this.router.navigate(['/plannings']);
    },
    error: (err: any) => {
      console.error('Error adding planning', err);

      if (err?.error?.message) {
        alert(err.error.message);
      } else if (err?.error?.messages) {
        alert(JSON.stringify(err.error.messages, null, 2));
      } else {
        alert('Error while adding planning');
      }
    }
  });
}
}
