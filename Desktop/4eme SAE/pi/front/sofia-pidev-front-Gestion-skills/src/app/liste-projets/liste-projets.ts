// src/app/components/liste-projets/liste-projets.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { ProjetService } from '../services/projet.service';
import { Projet } from '../models/projet';

@Component({
  selector: 'app-liste-projets',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, RouterLinkActive],
  templateUrl: './liste-projets.html',
  styleUrls: ['./liste-projets.css']
})
export class ListeProjetsComponent implements OnInit {

  projets: Projet[] = [];
  projetsFiltres: Projet[] = [];

  error: string | null = null;
  isLoading: boolean = true;

  domains: string[] = [
    'WEB', 'MOBILE', 'DATA_SCIENCE', 'IA', 'DEVOPS',
    'CYBERSECURITY', 'CLOUD_COMPUTING', 'GAME_DEV',
    'IOT', 'BIG_DATA', 'BLOCKCHAIN'
  ];

  filters = {
    search: '',
    domaine: '',
    dateDebut: ''
  };

  showFilters: boolean = false;

  stats = {
    total: 0,
    affiches: 0
  };

  expandedProjectId: number | null = null;
  loadingTasksFor: number | null = null;

  constructor(
    private projetService: ProjetService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('ListeProjetsComponent chargé ✅');
    this.loadProjets();
  }

  loadProjets(): void {
    this.isLoading = true;
    this.error = null;

    this.projetService.getProjets()
      .pipe(
        finalize(() => {
          // ✅ garantit que le loading s'arrête toujours (success ou error)
          this.isLoading = false;
          // ✅ force Angular à rafraîchir la vue immédiatement
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data: any) => {
          console.log("✅ Réponse brute de l'API:", data);

          // GESTION DES DIFFÉRENTS FORMATS DE RÉPONSE
          if (Array.isArray(data)) {
            this.projets = data;
          }
          else if (data && data.content && Array.isArray(data.content)) {
            this.projets = data.content;
          }
          else if (data && data.data && Array.isArray(data.data)) {
            this.projets = data.data;
          }
          else if (data && data.projets && Array.isArray(data.projets)) {
            this.projets = data.projets;
          }
          else if (data && typeof data === 'object') {
            const possibleArrays = Object.values(data).filter(val => Array.isArray(val));
            this.projets = possibleArrays.length > 0 ? (possibleArrays[0] as Projet[]) : [];
          }
          else {
            this.projets = [];
          }

          this.projetsFiltres = [...this.projets];
          this.updateStats();

          console.log("📋 Projets après traitement:", this.projets);
        },
        error: (err) => {
          console.error('❌ Erreur:', err);
          this.error = 'Impossible de charger les projets. Vérifiez que le serveur est accessible.';
          this.projets = [];
          this.projetsFiltres = [];
          this.updateStats();
        }
      });
  }

  applyFilters(): void {
    this.projetsFiltres = this.projets.filter(projet => {
      const matchesSearch = !this.filters.search ||
        projet.title.toLowerCase().includes(this.filters.search.toLowerCase()) ||
        (projet.description && projet.description.toLowerCase().includes(this.filters.search.toLowerCase()));

      const matchesDomaine = !this.filters.domaine ||
        projet.domaine === this.filters.domaine;

      const matchesDateDebut = !this.filters.dateDebut ||
        new Date(this.formatDate(projet.date)) >= new Date(this.filters.dateDebut);

      return matchesSearch && matchesDomaine && matchesDateDebut;
    });

    this.updateStats();
  }

  resetFilters(): void {
    this.filters = { search: '', domaine: '', dateDebut: '' };
    this.projetsFiltres = [...this.projets];
    this.updateStats();
  }

  private updateStats(): void {
    this.stats.total = this.projets.length;
    this.stats.affiches = this.projetsFiltres.length;
  }

  hasActiveFilters(): boolean {
    return !!(this.filters.search || this.filters.domaine || this.filters.dateDebut);
  }

  formatDate(date: any): string {
    if (!date) return '';

    if (Array.isArray(date)) {
      return `${date[0]}-${String(date[1]).padStart(2, '0')}-${String(date[2]).padStart(2, '0')}`;
    }
    if (typeof date === 'string') {
      return date.split('T')[0];
    }
    return String(date);
  }

  toggleDetails(projet: Projet): void {
    if (!projet.id) return;

    if (this.expandedProjectId === projet.id) {
      this.expandedProjectId = null;
      return;
    }

    this.expandedProjectId = projet.id;

    if (projet.tasks) {
      return;
    }

    this.loadingTasksFor = projet.id;
    this.projetService.getTasksByProjectId(projet.id).subscribe({
      next: (tasks) => {
        projet.tasks = tasks || [];
        this.loadingTasksFor = null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement tâches:', err);
        projet.tasks = [];
        this.loadingTasksFor = null;
        this.cdr.detectChanges();
      }
    });
  }

  applyToProject(id: number | undefined) {
    if (id) alert('Candidature envoyée avec succès !');
  }
}
