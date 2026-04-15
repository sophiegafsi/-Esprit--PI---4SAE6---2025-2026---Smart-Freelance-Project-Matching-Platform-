import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Evaluation } from '../models/evaluation.model';

interface FrontConfig {
  evaluationBaseUrl?: string;
  rewardBaseUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class EvaluationService {
  private readonly fallbackEvaluationUrl = 'http://localhost:8085';
  private readonly storageKey = 'freelink-front-config';

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Evaluation[]> {
    return this.http.get<Evaluation[]>(`${this.evaluationBaseUrl()}/evaluations/all`);
  }

  create(payload: Evaluation): Observable<Evaluation> {
    return this.http.post<Evaluation>(`${this.evaluationBaseUrl()}/evaluations/add`, payload);
  }

  getById(id: number): Observable<Evaluation> {
    return this.http.get<Evaluation>(`${this.evaluationBaseUrl()}/evaluations/${id}`);
  }

  getByUser(email: string): Observable<Evaluation[]> {
    return this.http.get<Evaluation[]>(`${this.evaluationBaseUrl()}/evaluations/user/${encodeURIComponent(email)}`);
  }

  update(id: number, payload: Evaluation): Observable<Evaluation> {
    return this.http.put<Evaluation>(`${this.evaluationBaseUrl()}/evaluations/update/${id}`, payload);
  }

  deleteById(id: number): Observable<void> {
    return this.http.delete<void>(`${this.evaluationBaseUrl()}/evaluations/delete/${id}`);
  }

  getAverageScore(userName: string): Observable<number> {
    return this.http.get<number>(`${this.evaluationBaseUrl()}/evaluations/average/${encodeURIComponent(userName)}`);
  }

  private evaluationBaseUrl(): string {
    if (typeof localStorage === 'undefined') {
      return this.fallbackEvaluationUrl;
    }

    try {
      const parsed = JSON.parse(localStorage.getItem(this.storageKey) || '{}') as FrontConfig;
      const value = String(parsed.evaluationBaseUrl || '').trim();
      return value || this.fallbackEvaluationUrl;
    } catch {
      return this.fallbackEvaluationUrl;
    }
  }
}
