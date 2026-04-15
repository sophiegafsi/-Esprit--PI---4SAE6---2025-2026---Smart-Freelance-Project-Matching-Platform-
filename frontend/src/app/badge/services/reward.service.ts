import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RewardHistoryItem } from '../models/reward-history.model';

interface FrontConfig {
  evaluationBaseUrl?: string;
  rewardBaseUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class RewardService {
  private readonly fallbackRewardUrl = 'http://localhost:8094';
  private readonly storageKey = 'freelink-front-config';

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`${this.rewardBaseUrl()}/api/rewards/dashboard`);
  }

  getRewards(): Observable<Record<string, unknown>[]> {
    return this.http.get<Record<string, unknown>[]>(`${this.rewardBaseUrl()}/api/recompenses`);
  }

  assignPendingRewards(): Observable<{ assignedRewards?: number }> {
    return this.http.post<{ assignedRewards?: number }>(`${this.rewardBaseUrl()}/api/rewards/assign-pending-rewards`, {});
  }

  recalculateLevels(): Observable<{ message?: string }> {
    return this.http.post<{ message?: string }>(`${this.rewardBaseUrl()}/api/rewards/recalculate-levels`, {});
  }

  getHistory(email?: string): Observable<RewardHistoryItem[]> {
    const query = email ? `?email=${encodeURIComponent(email)}` : '';
    return this.http.get<RewardHistoryItem[]>(`${this.rewardBaseUrl()}/api/rewards/history${query}`);
  }

  processEvaluation(payload: Record<string, unknown>): Observable<Record<string, unknown>> {
    return this.http.post<Record<string, unknown>>(`${this.rewardBaseUrl()}/api/rewards/process-evaluation`, payload);
  }

  downloadCertificate(historyId: number): Observable<Blob> {
    return this.http.get(`${this.rewardBaseUrl()}/api/rewards/certificates/${historyId}`, {
      responseType: 'blob'
    });
  }

  resendCertificateEmail(historyId: number, recipientEmail?: string): Observable<string> {
    const query = recipientEmail?.trim()
      ? `?recipientEmail=${encodeURIComponent(recipientEmail.trim())}`
      : '';

    return this.http.post(`${this.rewardBaseUrl()}/api/rewards/certificates/${historyId}/resend-email${query}`, null, {
      responseType: 'text'
    });
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
