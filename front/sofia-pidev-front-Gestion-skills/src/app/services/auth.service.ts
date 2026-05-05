import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, from } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    // Keycloak Token Endpoint
    private keycloakUrl = 'http://localhost:8080/realms/freelink-realm/protocol/openid-connect/token';
    private clientId = 'angular-frontend'; // Public client configured in freelink-realm.json

    private authState = new BehaviorSubject<boolean>(this.isLoggedIn());
    authState$ = this.authState.asObservable();

    private currentUserSubject = new BehaviorSubject<any>(null);
    currentUser$ = this.currentUserSubject.asObservable();

    private refreshTimer: any = null;

    constructor(private http: HttpClient) {
        // On service init, schedule a refresh if already logged in
        if (this.isLoggedIn()) {
            this.scheduleTokenRefresh();
        }
    }

    // Direct access to User Service (Routed through Gateway)
    private userApiUrl = 'http://localhost:8081/user/api/users';

    signup(userData: any): Observable<any> {
        return this.http.post(`${this.userApiUrl}/register`, userData);
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${this.userApiUrl}/forgot-password`, email);
    }

    getCurrentUser(): Observable<any> {
        const request = this.http.get(`${this.userApiUrl}/me`);
        request.subscribe({
            next: (user) => this.currentUserSubject.next(user),
            error: () => this.currentUserSubject.next(null)
        });
        return request;
    }

    getCurrentUserValue(): any {
        return this.currentUserSubject.value;
    }

    loadCurrentUser(): void {
        if (this.isLoggedIn()) {
            this.getCurrentUser().subscribe();
        }
    }

    isFreelancer(): boolean {
        const user = this.currentUserSubject.value;
        if (!user || !user.role) return false;
        return user.role.split(',').map((r: string) => r.trim().toLowerCase()).includes('freelancer');
    }

    isAdmin(): boolean {
        const user = this.currentUserSubject.value;
        if (!user || !user.role) return false;
        return user.role.split(',').map((r: string) => r.trim().toLowerCase()).includes('admin');
    }

    isClient(): boolean {
        const user = this.currentUserSubject.value;
        if (!user || !user.role) return false;
        return user.role.split(',').map((r: string) => r.trim().toLowerCase()).includes('client');
    }

    updateUser(user: any): Observable<any> {
        const request = this.http.put(`${this.userApiUrl}/${user.id}`, user);
        request.subscribe({
            next: (updatedUser) => this.currentUserSubject.next(updatedUser)
        });
        return request;
    }

    getAllUsers(): Observable<any> {
        return this.http.get(`${this.userApiUrl}`);
    }

    deleteAccount(userId: string): Observable<any> {
        return this.http.delete(`${this.userApiUrl}/${userId}`);
    }

    getUserById(userId: string): Observable<any> {
        return this.http.get(`${this.userApiUrl}/${userId}`);
    }

    becomeFreelancer(profile: any): Observable<any> {
        return this.http.post(`${this.userApiUrl}/become-freelancer`, profile);
    }

    login(email: string, password: string): Observable<any> {
        const body = new URLSearchParams();
        body.set('client_id', this.clientId);
        body.set('grant_type', 'password');
        body.set('username', email);
        body.set('password', password);

        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        return this.http.post(this.keycloakUrl, body.toString(), { headers });
    }

    /** Silently refresh using the stored refresh_token */
    refreshToken(): Observable<any> {
        const refreshToken = typeof localStorage !== 'undefined' ? localStorage.getItem('refresh_token') : null;
        if (!refreshToken) {
            this.logout();
            throw new Error('No refresh token available');
        }

        const body = new URLSearchParams();
        body.set('client_id', this.clientId);
        body.set('grant_type', 'refresh_token');
        body.set('refresh_token', refreshToken);

        const headers = new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' });

        return this.http.post(this.keycloakUrl, body.toString(), { headers }).pipe(
            tap((tokens: any) => {
                if (tokens.access_token) {
                    localStorage.setItem('access_token', tokens.access_token);
                }
                if (tokens.refresh_token) {
                    localStorage.setItem('refresh_token', tokens.refresh_token);
                }
                this.authState.next(true);
                this.scheduleTokenRefresh();
            })
        );
    }

    saveToken(tokenResponse: any): void {
        if (typeof localStorage !== 'undefined') {
            // Accept either a raw token string or a full token response object
            const accessToken = typeof tokenResponse === 'string' ? tokenResponse : tokenResponse.access_token;
            const refreshToken = typeof tokenResponse === 'string' ? null : tokenResponse.refresh_token;

            if (accessToken) localStorage.setItem('access_token', accessToken);
            if (refreshToken) localStorage.setItem('refresh_token', refreshToken);

            this.authState.next(true);
            this.scheduleTokenRefresh();
            this.loadCurrentUser();
        }
    }

    logout(): void {
        if (typeof localStorage !== 'undefined') {
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            this.authState.next(false);
            this.currentUserSubject.next(null);
        }
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
            this.refreshTimer = null;
        }
    }

    isLoggedIn(): boolean {
        if (typeof localStorage !== 'undefined') {
            return !!localStorage.getItem('access_token');
        }
        return false;
    }

    /** Schedule a silent token refresh 60 seconds before the access token expires */
    private scheduleTokenRefresh(): void {
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
            this.refreshTimer = null;
        }

        const token = typeof localStorage !== 'undefined' ? localStorage.getItem('access_token') : null;
        if (!token) return;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const expiresAt = payload.exp * 1000; // ms
            const now = Date.now();
            const delay = expiresAt - now - 60_000; // refresh 60s before expiry

            if (delay > 0) {
                this.refreshTimer = setTimeout(() => {
                    this.refreshToken().subscribe({
                        error: () => this.logout() // If refresh fails, log out cleanly
                    });
                }, delay);
            } else {
                // Token already expired or about to — refresh immediately
                this.refreshToken().subscribe({
                    error: () => this.logout()
                });
            }
        } catch {
            // Malformed token — ignore
        }
    }
}
