import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { PlanningService } from '../../services/planning';

@Component({
  selector: 'app-list-planning',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './list-planning.html',
  styleUrl: './list-planning.css'
})
export class ListPlanning implements OnInit {
  plannings: Planning[] = [];

  constructor(private planningService: PlanningService) {}

  ngOnInit(): void {
    this.loadPlannings();
  }

  loadPlannings(): void {
    this.planningService.getAllPlannings().subscribe({
      next: (data: Planning[]) => {
        console.log('PLANNINGS =', data);
        this.plannings = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement plannings', err);
      }
    });
  }

  deletePlanning(id: number | undefined): void {
    if (!id) return;

    if (confirm('Are you sure you want to delete this planning?')) {
      this.planningService.deletePlanning(id).subscribe({
        next: () => this.loadPlannings(),
        error: (err: any) => console.error('Erreur suppression planning', err)
      });
    }
  }
}