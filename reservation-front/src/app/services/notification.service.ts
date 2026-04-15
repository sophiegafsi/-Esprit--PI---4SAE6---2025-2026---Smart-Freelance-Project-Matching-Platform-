import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, switchMap, timer } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = '/api/notifications';
  
  // State for the currently faked logged in user
  private currentUserIdSubject = new BehaviorSubject<string>('DaliClient');
  public currentUserId$ = this.currentUserIdSubject.asObservable();

  private currentUserRoleSubject = new BehaviorSubject<'CLIENT' | 'FREELANCER'>('CLIENT');
  public currentUserRole$ = this.currentUserRoleSubject.asObservable();

  constructor(private http: HttpClient) { }

  setCurrentUser(userId: string, role: 'CLIENT' | 'FREELANCER' = 'CLIENT') {
    this.currentUserIdSubject.next(userId);
    this.currentUserRoleSubject.next(role);
  }

  getCurrentUser(): string {
    return this.currentUserIdSubject.getValue();
  }

  getCurrentRole(): 'CLIENT' | 'FREELANCER' {
    return this.currentUserRoleSubject.getValue();
  }

  getNotifications(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}`);
  }

  getUnreadCount(userId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/user/${userId}/unread-count`);
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${id}/read`, {});
  }
}
