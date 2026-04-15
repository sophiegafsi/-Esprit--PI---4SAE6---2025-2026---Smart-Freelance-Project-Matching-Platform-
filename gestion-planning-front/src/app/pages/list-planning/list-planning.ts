import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Planning } from '../../models/planning.model';
import { PlanningService } from '../../services/planning';
import { PopupService } from '../../services/popup.service';
import { PlanningEfficiency } from '../../models/planning-efficiency.model';

@Component({
  selector: 'app-list-planning',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './list-planning.html',
  styleUrl: './list-planning.css'
})
export class ListPlanning implements OnInit {
  plannings: Planning[] = [];
  weightedProgressMap: { [key: number]: number } = {};
  efficiencyMap: { [key: number]: PlanningEfficiency } = {};
  searchKeyword: string = '';

  constructor(
    private planningService: PlanningService,
    private popupService: PopupService
  ) {}

  ngOnInit(): void {
    this.loadPlannings();
  }

  loadPlannings(): void {
    this.planningService.getAllPlannings().subscribe({
      next: (data: Planning[]) => {
        this.plannings = data;
        this.loadExtraData();
      },
      error: (err: any) => {
        console.error('Erreur chargement plannings', err);
        this.popupService.error('Error', 'Unable to load plannings.');
      }
    });
  }

  loadExtraData(): void {
    this.weightedProgressMap = {};
    this.efficiencyMap = {};

    this.plannings.forEach((planning) => {
      if (planning.id) {
        this.planningService.getPlanningWeightedProgress(planning.id).subscribe({
          next: (progressData) => {
            this.weightedProgressMap[planning.id!] = progressData.weightedProgress;
          },
          error: () => {
            this.weightedProgressMap[planning.id!] = 0;
          }
        });

        this.planningService.getPlanningEfficiency(planning.id).subscribe({
          next: (efficiencyData) => {
            this.efficiencyMap[planning.id!] = efficiencyData;
          },
          error: () => {
            // rien
          }
        });
      }
    });
  }

  onSearchChange(): void {
    const keyword = this.searchKeyword.trim();

    if (!keyword) {
      this.loadPlannings();
      return;
    }

    this.planningService.searchPlannings(keyword).subscribe({
      next: (data: Planning[]) => {
        this.plannings = data;
        this.loadExtraData();
      },
      error: (err: any) => {
        console.error('Erreur recherche planning', err);
      }
    });
  }

  clearSearch(): void {
    this.searchKeyword = '';
    this.loadPlannings();
  }

  deletePlanning(id: number | undefined): void {
    if (!id) return;

    this.popupService.confirm(
      'Delete Planning',
      'Are you sure you want to delete this planning?',
      () => {
        this.planningService.deletePlanning(id).subscribe({
          next: () => {
            this.loadPlannings();
            this.popupService.success('Deleted', 'Planning deleted successfully.');
          },
          error: (err: any) => {
            console.error('Erreur suppression planning', err);
            this.popupService.error('Error', 'Unable to delete planning.');
          }
        });
      }
    );
  }
}