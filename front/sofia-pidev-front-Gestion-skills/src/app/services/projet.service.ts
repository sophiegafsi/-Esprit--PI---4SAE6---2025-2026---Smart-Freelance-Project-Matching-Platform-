// src/app/services/projet.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Projet, Task, ProjetDetaille } from '../models/projet';

@Injectable({
  providedIn: 'root'
})
export class ProjetService {
  private apiUrl = 'http://localhost:8081/projet';  // Microservice Projet via Gateway

  constructor(private http: HttpClient) { }

  // ✅ AJOUTER CETTE MÉTHODE
  getApiUrl(): string {
    return this.apiUrl;
  }

  getProjets(): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.apiUrl}/api/projets/allprojets`).pipe(
      catchError(this.handleError)
    );
  }

  getProjetsByClient(clientId: string): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.apiUrl}/api/projets/client/${clientId}`).pipe(
      catchError(this.handleError)
    );
  }

  getProjetById(id: number): Observable<Projet> {
    return this.http.get<Projet>(`${this.apiUrl}/api/projets/getprojet/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  addProject(project: Projet): Observable<Projet> {
    return this.http.post<Projet>(`${this.apiUrl}/api/projets/addprojet`, project).pipe(
      catchError(this.handleError)
    );
  }

  deleteProjet(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/api/projets/deleteprojet/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getTasksByProjectId(projectId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/api/projets/${projectId}/taches/alltaches`).pipe(
      catchError(this.handleError)
    );
  }

  updateProjet(id: number, project: Projet): Observable<Projet> {
    return this.http.put<Projet>(`${this.apiUrl}/api/projets/updateprojet/${id}`, project).pipe(
      catchError(this.handleError)
    );
  }

  addTaskToProject(projectId: number, task: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/projets/${projectId}/taches/addtache`, task).pipe(
      catchError(this.handleError)
    );
  }

  getTaskById(projectId: number, taskId: number): Observable<ProjetDetaille> {
    return this.http.get<ProjetDetaille>(`${this.apiUrl}/api/projets/${projectId}/taches/gettache/${taskId}`).pipe(
      catchError(this.handleError)
    );
  }

  updateTask(projectId: number, taskId: number, task: ProjetDetaille): Observable<ProjetDetaille> {
    return this.http.put<ProjetDetaille>(`${this.apiUrl}/api/projets/${projectId}/taches/updatetache/${taskId}`, task).pipe(
      catchError(this.handleError)
    );
  }

  deleteTask(projectId: number, taskId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/api/projets/${projectId}/taches/deletetache/${taskId}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('❌ Erreur détaillée:', error);
    return throwError(() => error);
  }
  calculateDevis(projetId: number, deadline?: string) {
    return this.http.post<any>(`${this.apiUrl}/api/devis/calculate`, {
      projetId,
      deadline: deadline || null
    });
  }
  getKanbanTasks() {
    return this.http.get<any[]>(`${this.apiUrl}/api/kanban/tasks`);
  }
}
