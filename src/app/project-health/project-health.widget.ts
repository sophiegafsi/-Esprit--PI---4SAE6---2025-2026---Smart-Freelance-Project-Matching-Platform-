import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-project-health-widget',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="health-card">
      <div class="health-head">
        <h3>📈 Project Health</h3>

        <span
          class="badge"
          [class.ok]="score >= 75"
          [class.warn]="score >= 45 && score < 75"
          [class.bad]="score < 45"
        >
          {{ score }}/100
        </span>
      </div>

      <!-- Résultat -->
      <div class="health-body">
        <div class="row">
          <span class="label">Tâches totales</span>
          <span class="value">{{ totalTasks }}</span>
        </div>

        <div class="row">
          <span class="label">En retard</span>
          <span class="value danger">{{ overdueTasks }}</span>
        </div>

        <div class="row">
          <span class="label">Urgentes (≤ 2j)</span>
          <span class="value warn">{{ urgentTasks }}</span>
        </div>

        <p class="hint">{{ message }}</p>
      </div>
    </div>
  `,
  styles: [`
    .health-card{
      border-radius:16px;
      padding:18px;
      border:1px solid rgba(255,255,255,0.10);
      background: rgba(255,255,255,0.04);
      margin-top: 14px;
      color: white;
    }
    .health-head{
      display:flex; justify-content:space-between; align-items:center;
      margin-bottom: 12px;
    }
    .health-head h3{ margin:0; font-size:16px; font-weight:900; }

    .badge{
      padding:6px 10px;
      border-radius:999px;
      font-weight:900;
      font-size:12px;
      border:1px solid rgba(255,255,255,0.12);
      background: rgba(255,255,255,0.06);
    }
    .badge.ok{ border-color: rgba(46,204,113,0.35); background: rgba(46,204,113,0.12); }
    .badge.warn{ border-color: rgba(243,156,18,0.35); background: rgba(243,156,18,0.12); }
    .badge.bad{ border-color: rgba(231,76,60,0.35); background: rgba(231,76,60,0.12); }

    .health-body{ display:flex; flex-direction:column; gap:10px; }
    .row{ display:flex; justify-content:space-between; font-weight:800; }
    .label{ color: rgba(255,255,255,0.65); }
    .value{ font-weight:900; }
    .value.danger{ color:#ff6b6b; }
    .value.warn{ color:#ffb74d; }

    .hint{
      margin: 10px 0 0;
      font-size: 13px;
      font-weight: 800;
      color: rgba(255,255,255,0.75);
      border-left: 4px solid rgba(47,128,237,0.6);
      padding-left: 10px;
    }
  `]
})
export class ProjectHealthWidgetComponent implements OnChanges {

  /**
   * ✅ On reçoit directement les tasks depuis projet-detail
   */
  @Input() tasks: any[] = [];

  totalTasks = 0;
  overdueTasks = 0;
  urgentTasks = 0;

  score = 0;
  message = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tasks']) {
      this.computeFromTasks(this.tasks || []);
    }
  }

  private computeFromTasks(list: any[]): void {
    this.totalTasks = list.length;

    const now = new Date();
    this.overdueTasks = 0;
    this.urgentTasks = 0;

    for (const t of list) {
      if (!t.deadline) continue;
      const d = new Date(t.deadline);
      const diffDays = Math.ceil((d.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

      if (diffDays < 0) this.overdueTasks++;
      else if (diffDays <= 2) this.urgentTasks++;
    }

    const penalty = (this.overdueTasks * 25) + (this.urgentTasks * 10);
    this.score = Math.max(0, Math.min(100, 100 - penalty));

    if (this.totalTasks === 0) {
      this.message = "Aucune tâche : ajoute des tâches pour mieux suivre l'avancement.";
    } else if (this.overdueTasks > 0) {
      this.message = "Attention : il y a des tâches en retard. Priorise-les en premier.";
    } else if (this.urgentTasks > 0) {
      this.message = "Certaines tâches sont urgentes (≤ 2 jours). Il faut agir rapidement.";
    } else {
      this.message = "Tout est sous contrôle. Les deadlines sont correctes.";
    }
  }
}
