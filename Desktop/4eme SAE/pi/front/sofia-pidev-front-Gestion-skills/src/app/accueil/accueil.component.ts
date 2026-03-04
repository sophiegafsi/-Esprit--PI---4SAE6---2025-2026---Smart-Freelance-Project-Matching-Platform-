import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { CandidatureService } from '../services/candidature.service';
import { Project } from '../models/project.model';

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

  constructor(
    private authService: AuthService,
    private router: Router,
    private candidatureService: CandidatureService
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
}
