// src/app/components/dashboard/dashboard.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProjetService } from '../services/projet.service';
import { Projet, ProjetDetaille } from '../models/projet';
import { AuthService } from '../services/auth.service';

import { forkJoin, of } from 'rxjs';
import { catchError, finalize, map } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  projets: Projet[] = [];

  isLoading = true;
  error: string | null = null;

  stats = {
    totalProjects: 0,
    totalTasks: 0,
    applications: 24,
    activeUsers: 156
  };

  recentActivities = [
    { icon: '📌', title: 'New project created', time: '2 minutes ago' },
    { icon: '✅', title: 'Task completed', time: '15 minutes ago' },
    { icon: '👤', title: 'New user registered', time: '1 hour ago' },
    { icon: '💳', title: 'Payment processed', time: '2 hours ago' },
    { icon: '📋', title: 'Application submitted', time: '3 hours ago' }
  ];

  constructor(
    private projetService: ProjetService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.isLoading = true;
    this.error = null;

    this.authService.getCurrentUser().subscribe(user => {
      if (!user) {
        this.error = 'Utilisateur non connecté.';
        this.isLoading = false;
        this.cdr.detectChanges();
        return;
      }

      if (this.authService.isClient() && !this.authService.isAdmin()) {
        this.projetService.getProjetsByClient(user.id)
          .pipe(
            finalize(() => {
              this.isLoading = false;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: (data: Projet[]) => {
              this.projets = data || [];
              this.stats.totalProjects = this.projets.length;
              this.loadTasksForProjects();
            },
            error: (err) => {
              console.error('Error:', err);
              this.error = 'Impossible de charger vos projets.';
              this.projets = [];
              this.stats.totalProjects = 0;
              this.stats.totalTasks = 0;
            }
          });
      } else {
        // Admin or fallback
        this.projetService.getProjets()
          .pipe(
            finalize(() => {
              this.isLoading = false;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: (data: Projet[]) => {
              this.projets = data || [];
              this.stats.totalProjects = this.projets.length;
              this.loadTasksForProjects();
            },
            error: (err) => {
              console.error('Error:', err);
              this.error = 'Impossible de charger tous les projets.';
              this.projets = [];
              this.stats.totalProjects = 0;
              this.stats.totalTasks = 0;
            }
          });
      }
    });
  }

  loadTasksForProjects(): void {
    if (!this.projets.length) {
      this.stats.totalTasks = 0;
      return;
    }

    const calls = this.projets.map(p => {
      if (!p.id) return of({ projectId: undefined, tasks: [] as ProjetDetaille[] });

      return this.projetService.getTasksByProjectId(p.id).pipe(
        map(tasks => ({ projectId: p.id, tasks: tasks || [] })),
        catchError(err => {
          console.error(`Error loading tasks for project ${p.id}:`, err);
          return of({ projectId: p.id, tasks: [] as ProjetDetaille[] });
        })
      );
    });

    forkJoin(calls).subscribe(results => {
      let total = 0;

      for (const r of results) {
        const proj = this.projets.find(p => p.id === r.projectId);
        if (proj) {
          proj.tasks = r.tasks;
          total += r.tasks.length;
        }
      }

      this.stats.totalTasks = total;
      this.cdr.detectChanges();
    });
  }

  deleteProject(id: number | undefined): void {
    if (!id) return;

    if (confirm('Freelink Warning: Are you sure you want to delete this project? This will permanently delete all associated freelancer applications and contracts.')) {
      this.projetService.deleteProjet(id).subscribe({
        next: () => {
          this.projets = this.projets.filter(p => p.id !== id);
          this.stats.totalProjects = this.projets.length;

          // recalcul totalTasks
          let total = 0;
          this.projets.forEach(p => total += (p.tasks?.length || 0));
          this.stats.totalTasks = total;

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error:', err);
          alert('Error deleting project');
        }
      });
    }
  }

  // ✅✅✅ HERE: delete task REAL
  deleteTask(projectId: number | undefined, taskId: number | undefined): void {
    if (!projectId || !taskId) return;

    if (!confirm('Are you sure you want to delete this task?')) return;

    this.projetService.deleteTask(projectId, taskId).subscribe({
      next: () => {
        // 1) remove task from UI
        const proj = this.projets.find(p => p.id === projectId);
        if (proj?.tasks) {
          proj.tasks = proj.tasks.filter(t => t.id !== taskId);
        }

        // 2) update stats
        this.stats.totalTasks = Math.max(0, this.stats.totalTasks - 1);

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error deleting task:', err);
        alert('Error deleting task. Check backend endpoint / permissions.');
      }
    });
  }

  exportData(event: Event): void {
    event.preventDefault();
    const dataStr = JSON.stringify(this.projets, null, 2);
    const dataUri = 'data:application/json;charset=utf-8,' + encodeURIComponent(dataStr);
    const exportFileDefaultName = `projects-export-${new Date().toISOString().slice(0, 10)}.json`;
    const linkElement = document.createElement('a');
    linkElement.setAttribute('href', dataUri);
    linkElement.setAttribute('download', exportFileDefaultName);
    linkElement.click();
  }

  refreshData(event: Event): void {
    event.preventDefault();
    this.loadProjects();
  }

  trackByProjectId(index: number, projet: Projet): number {
    return projet.id || index;
  }

  trackByTaskId(index: number, task: ProjetDetaille): number {
    return task.id || index;
  }
}
