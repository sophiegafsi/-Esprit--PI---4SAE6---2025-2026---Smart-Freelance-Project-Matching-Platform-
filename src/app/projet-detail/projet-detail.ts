// src/app/components/projet-detail/projet-detail.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ProjetService } from '../services/projet.service';
import { Projet, ProjetDetaille } from '../models/projet';

// ✅ Budget
import { DevisCalculatorComponent } from '../devis-calculator/devis-calculator.component';
import { DevisCalculatorService, DevisResult } from '../devis-calculator/devis-calculator.service';

// ✅ Project Health (widget)
import { ProjectHealthWidgetComponent } from '../project-health/project-health.widget';

@Component({
  selector: 'app-projet-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    DevisCalculatorComponent,
    ProjectHealthWidgetComponent
  ],
  templateUrl: './projet-detail.html',
  styleUrls: ['./projet-detail.css']
})
export class ProjetDetailComponent implements OnInit {
  projet: Projet | null = null;
  tasks: ProjetDetaille[] = [];
  loading = false;
  error: string | null = null;

  // ✅ Devis préchargé (pour afficher le budget plus vite)
  devis: DevisResult | null = null;
  devisError: string | null = null;
  devisLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private devisService: DevisCalculatorService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = Number(params['id']);

      if (!id || Number.isNaN(id)) {
        this.error = 'ID du projet invalide';
        return;
      }

      // ✅ Lancer tout en parallèle dès l'arrivée sur la page
      this.loadProjet(id);
      this.loadTasks(id);
      this.preloadDevis(id);
    });
  }

  /**
   * Charge les infos du projet
   */
  private loadProjet(id: number): void {
    this.loading = true;
    this.error = null;

    this.projetService.getProjetById(id).subscribe({
      next: (data) => {
        this.projet = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Erreur chargement projet', err);
        this.error = 'Projet non trouvé';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Charge les tâches du projet
   */
  private loadTasks(id: number): void {
    this.projetService.getTasksByProjectId(id).subscribe({
      next: (data) => {
        this.tasks = data || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Erreur chargement tâches', err);
        this.tasks = [];
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Précharge le devis dès l'entrée (pour éviter un gros temps d'attente après affichage)
   * Ici : on calcule uniquement par projetId (deadline optionnelle)
   */
  private preloadDevis(projetId: number): void {
    this.devisLoading = true;
    this.devisError = null;
    this.devis = null;

    this.devisService.calculerDevisDepuisBackend(projetId).subscribe({
      next: (res) => {
        this.devis = res;
        this.devisLoading = false;
        this.cdr.detectChanges();
      },
      error: (e: Error) => {
        this.devisError = e.message || 'Erreur lors du calcul du devis';
        this.devisLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  postuler(): void {
    alert('Application submitted for ' + (this.projet?.title || 'this project'));
  }

  retour(): void {
    this.router.navigate(['/projets']);
  }
}
