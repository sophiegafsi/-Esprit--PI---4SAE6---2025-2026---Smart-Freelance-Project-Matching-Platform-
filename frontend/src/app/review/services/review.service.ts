import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review } from '../models/review.model';

interface FrontConfig {
  evaluationBaseUrl?: string;
  rewardBaseUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly fallbackEvaluationUrl = 'http://localhost:8085';
  private readonly fallbackRewardUrl = 'http://localhost:8094';
  private readonly storageKey = 'freelink-front-config';

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.evaluationBaseUrl()}/review/all`);
  }

  getById(id: number): Observable<Review> {
    return this.http.get<Review>(`${this.evaluationBaseUrl()}/review/${id}`);
  }

  getByUser(email: string): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.evaluationBaseUrl()}/review/user/${encodeURIComponent(email)}`);
  }

  getByEvaluation(evaluationId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.evaluationBaseUrl()}/review/evaluation/${evaluationId}`);
  }

  create(payload: Review): Observable<Review> {
    return this.http.post<Review>(`${this.evaluationBaseUrl()}/review/add`, payload);
  }

  update(id: number, payload: Review): Observable<Review> {
    return this.http.put<Review>(`${this.evaluationBaseUrl()}/review/update/${id}`, payload);
  }

  deleteById(id: number): Observable<void> {
    return this.http.delete<void>(`${this.evaluationBaseUrl()}/review/delete/${id}`);
  }

  getSentimentStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.evaluationBaseUrl()}/review/sentiment-stats`);
  }

  analyzeSentiment(text: string): Observable<{ text: string; sentiment: string }> {
    return this.http.post<{ text: string; sentiment: string }>(
      `${this.evaluationBaseUrl()}/review/sentiment/analyze`,
      { text }
    );
  }

  exportHistoryPdf(historyId: number): Observable<Blob> {
    return this.http.get(`${this.rewardBaseUrl()}/api/rewards/certificates/${historyId}`, {
      responseType: 'blob'
    });
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

  private rewardBaseUrl(): string {
    if (typeof localStorage === 'undefined') {
      return this.fallbackRewardUrl;
    }

    try {
      const parsed = JSON.parse(localStorage.getItem(this.storageKey) || '{}') as FrontConfig;
      const value = String(parsed.rewardBaseUrl || '').trim();
      return value || this.fallbackRewardUrl;
    } catch {
      return this.fallbackRewardUrl;
    }
  }
}

