import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Badge, UserBadgeDTO } from '../models/badge.model';

interface FrontConfig {
  evaluationBaseUrl?: string;
  rewardBaseUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class BadgeService {
  private readonly fallbackRewardUrl = 'http://localhost:8081';
  private readonly storageKey = 'freelink-front-config';

  constructor(private readonly http: HttpClient) { }

  list(): Observable<Badge[]> {
    return this.http.get<Badge[]>(`${this.rewardBaseUrl()}/api/badges`);
  }

  getById(id: number): Observable<Badge> {
    return this.http.get<Badge>(`${this.rewardBaseUrl()}/api/badges/${id}`);
  }

  listActive(): Observable<Badge[]> {
    return this.http.get<Badge[]>(`${this.rewardBaseUrl()}/api/badges/active`);
  }

  create(payload: Badge): Observable<Badge> {
    return this.http.post<Badge>(`${this.rewardBaseUrl()}/api/badges`, payload);
  }

  update(id: number, payload: Badge): Observable<Badge> {
    return this.http.put<Badge>(`${this.rewardBaseUrl()}/api/badges/${id}`, payload);
  }

  deleteById(id: number): Observable<void> {
    return this.http.delete<void>(`${this.rewardBaseUrl()}/api/badges/${id}`);
  }

  assignById(id: number): Observable<{ message?: string, assignedRewards?: number }> {
    return this.http.post<{ message?: string, assignedRewards?: number }>(
      `${this.rewardBaseUrl()}/api/badges/${id}/assign`,
      {}
    );
  }

  getUserBadges(userName: string): Observable<UserBadgeDTO[]> {
    return this.http.get<UserBadgeDTO[]>(`${this.rewardBaseUrl()}/api/user-badges/badges/${userName}`);
  }

  getActiveBadges(userName: string): Observable<UserBadgeDTO[]> {
    return this.http.get<UserBadgeDTO[]>(`${this.rewardBaseUrl()}/api/user-badges/active/${userName}`);
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
