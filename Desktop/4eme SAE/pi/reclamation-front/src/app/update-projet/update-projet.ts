// src/app/components/update-projet/update-projet.ts

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjetService } from '../services/projet.service';
import { Projet } from '../models/projet';

@Component({
  selector: 'app-update-projet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './update-projet.html',
  styleUrls: ['./update-projet.css']
})
export class UpdateProjetComponent implements OnInit {

  // Date du jour utilisée pour limiter la sélection de date (min dans input date)
  today: string = new Date().toISOString().split('T')[0];

  // Liste des domaines disponibles pour le projet
  domains = [
    'WEB', 'MOBILE', 'DESKTOP', 'DATA_SCIENCE', 'IA',
    'DEVOPS', 'CYBERSECURITY', 'CLOUD_COMPUTING', 'GAME_DEV',
    'IOT', 'BIG_DATA', 'BLOCKCHAIN'
  ];

  // Objet projet initialisé avec des valeurs vides
  project: Projet = {
    title: '',
    description: '',
    date: '',
    domaine: ''
  };

  // Identifiant du projet récupéré depuis l'URL
  projectId: number | null = null;

  // Variables d’état
  loading: boolean = true;       // Indique si le projet est en cours de chargement
  submitting: boolean = false;  // Indique si le formulaire est en cours d’envoi
  error: string | null = null;   // Message d’erreur éventuel

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * Méthode appelée automatiquement à l'initialisation du composant
   * Permet de charger les données du projet
   */
  ngOnInit(): void {
    this.loadProject();
  }

  /**
   * Convertit différents formats de date vers le format attendu
   * par l'input HTML de type "date" (yyyy-MM-dd)
   */
  private normalizeDateForInput(date: any): string {

    if (!date) return '';

    // Cas où la date arrive sous forme de tableau [année, mois, jour]
    if (Array.isArray(date) && date.length >= 3) {
      const y = String(date[0]);
      const m = String(date[1]).padStart(2, '0');
      const d = String(date[2]).padStart(2, '0');
      return `${y}-${m}-${d}`;
    }

    // Cas ISO avec heure
    if (typeof date === 'string' && date.includes('T')) {
      return date.split('T')[0];
    }

    // Cas format dd/MM/yyyy
    if (typeof date === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(date)) {
      const [dd, mm, yyyy] = date.split('/');
      return `${yyyy}-${mm}-${dd}`;
    }

    // Cas déjà au bon format
    if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date;
    }

    // Tentative de conversion générique
    try {
      const dt = new Date(date);
      if (!isNaN(dt.getTime())) return dt.toISOString().split('T')[0];
    } catch {}

    return '';
  }

  /**
   * Charge les informations du projet à partir de son identifiant
   */
  loadProject(): void {

    // Récupération de l'id depuis l’URL
    const idParam = this.route.snapshot.paramMap.get('id');
    this.projectId = Number(idParam);

    // Vérification de validité de l'id
    if (!this.projectId || isNaN(this.projectId)) {
      this.error = 'Identifiant de projet invalide';
      this.loading = false;
      return;
    }

    // Appel au service pour récupérer le projet
    this.projetService.getProjetById(this.projectId).subscribe({
      next: (data) => {

        // Injection des données dans l’objet projet
        this.project = {
          ...data,
          date: this.normalizeDateForInput((data as any).date)
        };

        this.loading = false;

        // Forcer la mise à jour de la vue si nécessaire
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Impossible de charger le projet';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Méthode exécutée lors de la soumission du formulaire
   */
  onSubmit(): void {

    if (!this.projectId) return;

    this.submitting = true;

    // Préparation des données à envoyer
    const payload: Projet = {
      ...this.project,
      date: this.normalizeDateForInput(this.project.date)
    };

    // Appel du service pour mettre à jour le projet
    this.projetService.updateProjet(this.projectId, payload).subscribe({
      next: () => {
        this.submitting = false;

        // Redirection vers le dashboard après mise à jour
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.submitting = false;
        alert('Erreur lors de la mise à jour du projet');
      }
    });
  }

  /**
   * Annule l’opération et redirige vers le dashboard
   */
  annuler(): void {
    this.router.navigate(['/dashboard']);
  }
}
