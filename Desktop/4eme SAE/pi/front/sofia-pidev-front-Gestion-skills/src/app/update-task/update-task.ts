// src/app/components/update-task/update-task.component.ts

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjetService } from '../services/projet.service';
import { ProjetDetaille } from '../models/projet';

@Component({
  selector: 'app-update-task',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './update-task.html',
  styleUrls: ['./update-task.css']
})
export class UpdateTaskComponent implements OnInit {

  // Date du jour utilisée pour limiter la date minimale dans l'input (min)
  today: string = new Date().toISOString().split('T')[0];

  // Paramètres de route
  taskId: number | null = null;
  projetId: number | null = null;

  // Objet tâche initialisé avec des valeurs vides (évite undefined dans le template)
  task: ProjetDetaille = {
    taskname: '',
    description: '',
    deadline: ''
  };

  // États d’affichage
  dataLoaded: boolean = false;
  isLoading: boolean = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private cdr: ChangeDetectorRef
  ) { }

  /**
   * Méthode appelée automatiquement à l'initialisation du composant
   * Récupère projectId et taskId depuis l'URL puis charge la tâche.
   */
  ngOnInit(): void {
    this.projetId = Number(this.route.snapshot.paramMap.get('projectId'));
    this.taskId = Number(this.route.snapshot.paramMap.get('taskId'));

    console.log('📌 Paramètres:', { projetId: this.projetId, taskId: this.taskId });

    if (this.projetId && this.taskId) {
      this.loadTask();
    } else {
      this.error = 'Paramètres invalides';
    }
  }

  /**
   * Convertit différents formats de date vers le format attendu
   * par l'input HTML de type "date" : yyyy-MM-dd
   */
  private normalizeDateForInput(date: any): string {
    if (!date) return '';

    // Cas où le backend renvoie un tableau [yyyy, mm, dd]
    if (Array.isArray(date) && date.length >= 3) {
      const y = String(date[0]);
      const m = String(date[1]).padStart(2, '0');
      const d = String(date[2]).padStart(2, '0');
      return `${y}-${m}-${d}`;
    }

    // Cas où le backend renvoie une date ISO avec heure "2026-03-07T00:00:00"
    if (typeof date === 'string' && date.includes('T')) {
      return date.split('T')[0];
    }

    // Cas déjà au bon format "2026-03-07"
    if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date;
    }

    // Cas format français "07/03/2026" -> conversion en "2026-03-07"
    if (typeof date === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(date)) {
      const [dd, mm, yyyy] = date.split('/');
      return `${yyyy}-${mm}-${dd}`;
    }

    // Fallback : tentative de conversion générique
    try {
      const dt = new Date(date);
      if (!isNaN(dt.getTime())) return dt.toISOString().split('T')[0];
    } catch { }

    return '';
  }

  /**
   * Charge la tâche depuis l'API en utilisant projetId + taskId.
   * Normalise la deadline pour afficher correctement dans input[type=date].
   */
  loadTask(): void {
    if (!this.projetId || !this.taskId) return;

    this.isLoading = true;
    this.error = null;
    this.dataLoaded = false;

    this.projetService.getTaskById(this.projetId, this.taskId).subscribe({
      next: (data) => {
        console.log('✅ Tâche chargée:', data);

        // Normaliser deadline pour input date
        const normalizedDeadline = this.normalizeDateForInput((data as any).deadline);

        // Mettre à jour l'objet task
        this.task = {
          ...data,
          deadline: normalizedDeadline
        };

        this.dataLoaded = true;
        this.isLoading = false;

        // Forcer Angular à rendre les valeurs immédiatement (évite le "vide jusqu'au clic")
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Erreur:', err);
        this.error = 'Erreur de chargement';
        this.isLoading = false;
      }
    });
  }

  /**
   * Soumission du formulaire : vérifie les champs puis envoie la mise à jour.
   */
  onSubmit(): void {
    this.error = null;

    // Vérification simple côté front
    if (!this.task.taskname || !this.task.description || !this.task.deadline) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    if (!this.projetId || !this.taskId) return;

    // Préparer le payload avec une deadline au bon format
    const payload: ProjetDetaille = {
      ...this.task,
      deadline: this.normalizeDateForInput(this.task.deadline)
    };

    this.projetService.updateTask(this.projetId, this.taskId, payload).subscribe({
      next: () => {
        this.success = 'Tâche mise à jour !';

        // ✅ Redirection vers le dashboard après succès
        setTimeout(() => this.router.navigate(['/projet-dashboard']), 1200);
      },
      error: (err) => {
        console.error('❌ Erreur:', err);
        this.error = 'Erreur de mise à jour';
      }
    });
  }

  /**
   * Annule la modification et redirige vers le dashboard
   */
  onCancel(): void {
    this.router.navigate(['/projet-dashboard']);
  }
}
