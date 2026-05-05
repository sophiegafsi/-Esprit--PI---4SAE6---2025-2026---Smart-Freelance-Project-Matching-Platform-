import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { CandidatureService } from '../services/candidature.service';
import { Project } from '../models/project.model';
import { ProjetService } from '../services/projet.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-accueil',
  templateUrl: './accueil.component.html',
  styleUrls: ['./accueil.component.css']
})
export class AccueilComponent implements OnInit {
  isLoggedIn = false;
  user: any = null;
  projects: Project[] = [];
  loadingProjects = true;

  expandedProjectId: number | string | null = null;
  loadingTasksFor: number | string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private candidatureService: CandidatureService,
    private projetService: ProjetService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.authService.getCurrentUser().subscribe({
      next: (data) => {
        this.isLoggedIn = true;
        this.user = data;
      },
      error: () => {
        this.isLoggedIn = false;
      }
    });

    this.loadProjects();
  }

  loadProjects() {
    this.candidatureService.getAllProjects().subscribe({
      next: (data) => {
        // Only show projects that are OPEN
        this.projects = data.filter(p => !p.status || p.status === 'OPEN');
        this.loadingProjects = false;
      },
      error: (err) => {
        console.error('Failed to load projects', err);
        this.loadingProjects = false;
      }
    });
  }

  login() {
    this.router.navigate(['/login']);
  }

  logout() {
    this.authService.logout();
    this.isLoggedIn = false;
    this.user = null;
  }

  toggleDetails(project: any): void {
    if (!project.id) return;

    if (this.expandedProjectId === project.id) {
      this.expandedProjectId = null;
      return;
    }

    this.expandedProjectId = project.id;

    if (project.tasks) {
      return;
    }

    this.loadingTasksFor = project.id;
    this.projetService.getTasksByProjectId(project.id).subscribe({
      next: (tasks) => {
        project.tasks = tasks || [];
        this.loadingTasksFor = null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement tâches:', err);
        project.tasks = [];
        this.loadingTasksFor = null;
        this.cdr.detectChanges();
      }
    });
  }
}
