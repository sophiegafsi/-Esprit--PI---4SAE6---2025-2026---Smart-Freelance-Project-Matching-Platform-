// src/app/components/kanban-board/kanban-board.ts
import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProjetService } from '../services/projet.service';
import { Subscription, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-kanban-board',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="page">
      <div class="container">
        <div class="page-head">
          <h1>📋 Flow</h1>
          <p>Smart Kanban (auto) basé sur deadlines</p>
        </div>

        <div *ngIf="loading" class="loading-container">
          <div class="spinner"></div>
          <p>Chargement du board...</p>
        </div>

        <div *ngIf="!loading && error" class="error-box">
          <p>{{ error }}</p>
          <button class="btn ghost" (click)="loadTasks()">⟲ Réessayer</button>
        </div>

        <div class="kanban-board" *ngIf="!loading && !error">
          <div class="kanban-col" *ngFor="let col of columns">
            <div class="col-header" [style.background]="col.color">
              <h3>{{ col.title }}</h3>
              <span class="col-count">{{ getTasksByColumn(col.id).length }}</span>
            </div>

            <div class="col-content">
              <div class="empty-col" *ngIf="getTasksByColumn(col.id).length === 0">
                No tasks
              </div>

              <div class="task-card"
                   *ngFor="let task of getTasksByColumn(col.id)"
                   [style.border-left-color]="getPriorityColor(task)"
                   [class.overdue]="task.status === 'overdue'">

                <div class="task-header">
                  <h4>{{ task.taskname }}</h4>
                  <span class="task-priority" [style.background]="getPriorityColor(task)">
                    {{ task.priorite || 'Normal' }}
                  </span>
                </div>

                <p class="task-desc">{{ task.description | slice:0:80 }}...</p>

                <div class="task-footer">
                  <span class="task-date"
                        [class.urgent]="task.status === 'urgent'"
                        [class.overdueText]="task.status === 'overdue'">
                    ⏰ {{ task.deadline | date:'dd/MM' }}
                  </span>

                  <span class="task-project">
                    📁 Projet #{{ task.projetId }}
                  </span>
                </div>
              </div>

            </div>
          </div>
        </div>

      </div>
    </main>
  `,
  styles: [`
    .kanban-board { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; min-height:600px; }
    .kanban-col { background:rgba(255,255,255,0.05); border-radius:12px; overflow:hidden; border:1px solid rgba(255,255,255,0.1); }
    .col-header { padding:15px; display:flex; justify-content:space-between; align-items:center; }
    .col-header h3 { margin:0; font-size:16px; font-weight:700; }
    .col-count { background:rgba(0,0,0,0.3); padding:4px 10px; border-radius:20px; font-size:14px; font-weight:700; }

    .col-content { padding:15px; min-height:500px; max-height:600px; overflow-y:auto; }
    .empty-col { opacity:.6; font-size:12px; padding:10px; border:1px dashed rgba(255,255,255,0.15); border-radius:8px; text-align:center; }

    .task-card { background:rgba(255,255,255,0.1); border-left:4px solid; border-radius:8px; padding:15px; margin-bottom:12px; transition:.2s; }
    .task-card:hover { transform:translateY(-2px); box-shadow:0 5px 15px rgba(0,0,0,0.3); background:rgba(255,255,255,0.15); }

    .task-card.overdue { outline: 1px solid rgba(231, 76, 60, 0.35); }

    .task-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; }
    .task-header h4 { margin:0; font-size:15px; font-weight:700; }
    .task-priority { font-size:10px; padding:3px 8px; border-radius:20px; color:white; font-weight:700; }

    .task-desc { margin:0 0 12px; font-size:12px; color:rgba(255,255,255,0.7); line-height:1.4; }
    .task-footer { display:flex; justify-content:space-between; align-items:center; font-size:11px; }
    .task-date { color:rgba(255,255,255,0.5); }
    .task-date.urgent { color:#ffb74d; font-weight:900; }
    .task-date.overdueText { color:#ff6b6b; font-weight:900; }

    .task-project { background:rgba(255,255,255,0.1); padding:3px 8px; border-radius:4px; }

    .loading-container { text-align:center; padding:50px; }
    .spinner { width:40px; height:40px; border:3px solid rgba(255,255,255,0.1); border-radius:50%; border-top-color:#f2994a; animation:spin 1s linear infinite; margin:0 auto 20px; }
    .error-box { text-align:center; padding:30px; border:1px dashed rgba(255,255,255,0.2); border-radius:12px; }
    @keyframes spin { to { transform:rotate(360deg); } }

    @media (max-width:1000px){ .kanban-board{ grid-template-columns:repeat(2,1fr);} }
    @media (max-width:600px){ .kanban-board{ grid-template-columns:1fr;} }
  `]
})
export class KanbanBoardComponent implements OnInit, OnDestroy {

  tasks: any[] = [];
  loading = true;
  error: string | null = null;

  private autoRefreshSub?: Subscription;

  // ✅ Colonnes smart (ids = status backend)
  columns = [
    { id: 'todo', title: '📝 À faire (>7j)', color: '#3498db' },
    { id: 'inprogress', title: '⚡ En cours (2-7j)', color: '#f39c12' },
    { id: 'urgent', title: '🔥 Urgent (≤2j)', color: '#9b59b6' },
    { id: 'overdue', title: '⛔ En retard', color: '#e74c3c' }
  ];

  constructor(
    private projetService: ProjetService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTasks();

    // refresh كل 15 ثانية (تنجم تنقصها)
    this.autoRefreshSub = interval(15000)
      .pipe(startWith(0), switchMap(() => this.projetService.getKanbanTasks()))
      .subscribe({
        next: (data) => {
          this.tasks = data || [];
          this.loading = false;
          this.error = null;
          this.cdr.detectChanges();
        },
        error: () => {
          this.loading = false;
          this.error = 'Impossible de charger le Flow.';
          this.tasks = [];
          this.cdr.detectChanges();
        }
      });
  }

  ngOnDestroy(): void {
    this.autoRefreshSub?.unsubscribe();
  }

  loadTasks(): void {
    this.loading = true;
    this.error = null;

    this.projetService.getKanbanTasks().subscribe({
      next: (data) => {
        this.tasks = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.error = 'Impossible de charger le Flow.';
        this.tasks = [];
        this.cdr.detectChanges();
      }
    });
  }

  getTasksByColumn(columnId: string) {
    return this.tasks.filter(task => task.status === columnId);
  }

  getPriorityColor(task: any): string {
    const colors: any = {
      'Haute': '#e74c3c',
      'Moyenne': '#f39c12',
      'Normal': '#3498db'
    };
    return colors[task.priorite] || '#3498db';
  }
}
