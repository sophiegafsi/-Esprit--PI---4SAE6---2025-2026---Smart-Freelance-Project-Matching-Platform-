import { Component, OnInit } from '@angular/core';
import { CandidatureService } from '../../services/candidature.service';
import { AuthService } from '../../services/auth.service';
import { Project } from '../../models/project.model';

@Component({
  selector: 'app-client-projects',
  templateUrl: './client-projects.component.html',
  styleUrls: ['./client-projects.component.css']
})
export class ClientProjectsComponent implements OnInit {
  projects: Project[] = [];
  loading: boolean = true;
  errorMessage: string = '';

  constructor(
    private candidatureService: CandidatureService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(user => {
      if (user) {
        if (this.authService.isClient()) {
          this.loadProjects(user.id);
        } else {
          this.errorMessage = 'Freelink Access Denied: Client role is required to view your projects.';
          this.loading = false;
        }
      } else {
        this.errorMessage = 'Freelink: Please log in to your account to continue.';
        this.loading = false;
      }
    });
  }

  loadProjects(clientId: string): void {
    this.candidatureService.getProjectsByClient(clientId).subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Freelink Server Error: Failed to load your projects.';
        this.loading = false;
      }
    });
  }
}
