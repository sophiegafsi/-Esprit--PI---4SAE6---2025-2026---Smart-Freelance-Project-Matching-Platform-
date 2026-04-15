import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Planning } from '../models/planning.model';
import { PlanningProgress } from '../models/planning-progress.model';
import { PlanningWeightedProgress } from '../models/planning-weighted-progress.model';
import { PlanningDailyLoad } from '../models/planning-daily-load.model';
import { PlanningEfficiency } from '../models/planning-efficiency.model';

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

  searchPlannings(keyword: string): Observable<Planning[]> {
    return this.http.get<Planning[]>(`${this.apiUrl}/search?keyword=${keyword}`);
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

  getPlanningProgress(id: number): Observable<PlanningProgress> {
    return this.http.get<PlanningProgress>(`${this.apiUrl}/${id}/progress`);
  }

  getPlanningWeightedProgress(id: number): Observable<PlanningWeightedProgress> {
    return this.http.get<PlanningWeightedProgress>(`${this.apiUrl}/${id}/weighted-progress`);
  }

  getPlanningDailyLoad(id: number): Observable<PlanningDailyLoad[]> {
    return this.http.get<PlanningDailyLoad[]>(`${this.apiUrl}/${id}/daily-load`);
  }

  getPlanningEfficiency(id: number): Observable<PlanningEfficiency> {
    return this.http.get<PlanningEfficiency>(`${this.apiUrl}/${id}/efficiency`);
  }
}