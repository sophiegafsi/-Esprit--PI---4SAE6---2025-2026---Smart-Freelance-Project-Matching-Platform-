import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Planning } from '../models/planning.model';

@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  private apiUrl = 'http://localhost:8086/api/plannings';

  constructor(private http: HttpClient) {}

  getAllPlannings(): Observable<Planning[]> {
    return this.http.get<Planning[]>(this.apiUrl);
  }

  getPlanningById(id: number): Observable<Planning> {
    return this.http.get<Planning>(`${this.apiUrl}/${id}`);
  }

  addPlanning(planning: Planning): Observable<Planning> {
    return this.http.post<Planning>(this.apiUrl, planning);
  }

  updatePlanning(id: number, planning: Planning): Observable<Planning> {
    return this.http.put<Planning>(`${this.apiUrl}/${id}`, planning);
  }

  deletePlanning(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}