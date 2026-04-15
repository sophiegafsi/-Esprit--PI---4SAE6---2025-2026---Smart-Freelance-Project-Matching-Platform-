// src/app/services/project-health.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProjectHealthResponse {
  projetId: number;
  projetTitle: string;

  score: number;
  niveau: 'GREEN' | 'YELLOW' | 'RED';
  message: string;

  totalTasks: number;
  overdueTasks: number;
  urgentTasks: number;
  soonTasks: number;

  joursJusquaDeadlineProjet: number;
}

@Injectable({ providedIn: 'root' })
export class ProjectHealthService {
  private readonly apiUrl = 'http://localhost:8081/projet';

  constructor(private http: HttpClient) { }

  getProjectHealth(projetId: number): Observable<ProjectHealthResponse> {
    return this.http.get<ProjectHealthResponse>(`${this.apiUrl}/api/health/project/${projetId}`);
  }

  getAllProjectsHealth(): Observable<ProjectHealthResponse[]> {
    return this.http.get<ProjectHealthResponse[]>(`${this.apiUrl}/api/health/projects`);
  }
}
