import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Notification {
    id: string;
    userId: string;
    message: string;
    read: boolean;
    type: string;
    createdAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private apiUrl = 'http://localhost:8082/api/users';

    constructor(private http: HttpClient) { }

    getNotifications(userId: string): Observable<Notification[]> {
        return this.http.get<Notification[]>(`${this.apiUrl}/${userId}/notifications`);
    }

    markAsRead(notificationId: string): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/notifications/${notificationId}/read`, {});
    }
}
