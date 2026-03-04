import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

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
    // If your client is 'public' (standard for SPA), you don't need client_secret here.
    // If it's 'confidential', you do, but SPAs shouldn't hold secrets.
    // Assuming 'public' client for now or 'confidential' with secret (risk in SPA).
    // Given the previous backend config had a secret, this might be a confidential client.
    // For direct access grant from SPA, usually public client is preferred or BFF.
    // However, to make it work 'in the UI' simply:

    // private clientSecret = 'cuyjZO1oj5jHoefWmlGPoerryp8od9KP'; // DANGER: Exposing secret in frontend is bad practice.
    // Ideally, change client to 'public' in Keycloak or use BFF.
    // For now, I will try without secret (Public Client) first. 
    // If Keycloak requires secret, we might need to proxy or change Keycloak config.
    // Let's assume the user wants it working now. If 401, we might need the secret or backend proxy.

    constructor(private http: HttpClient) { }

    // API Gateway URL for User Service
    private userApiUrl = 'http://localhost:8082/api/users';

    signup(userData: any): Observable<any> {
        return this.http.post(`${this.userApiUrl}/register`, userData);
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${this.userApiUrl}/forgot-password`, email);
    }

    getCurrentUser(): Observable<any> {
        // Now using Bearer token from interceptor
        const request = this.http.get(`${this.userApiUrl}/me`);
        request.subscribe({
            next: (user) => this.currentUserSubject.next(user),
            error: () => this.currentUserSubject.next(null)
        });
        return request;
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
        // Assuming User Service has GET /users/{id}
        // Based on updateUser using /users/{id}, this should exist or be supported.
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
        // body.set('client_secret', this.clientSecret); // Uncomment if absolutely necessary and understood risk

        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        return this.http.post(this.keycloakUrl, body.toString(), { headers });
    }

    saveToken(token: string): void {
        if (typeof localStorage !== 'undefined') {
            localStorage.setItem('access_token', token);
            this.authState.next(true);
            this.loadCurrentUser();
        }
    }

    logout(): void {
        if (typeof localStorage !== 'undefined') {
            localStorage.removeItem('access_token');
            this.authState.next(false);
            this.currentUserSubject.next(null);
        }
        // Optional: Call Keycloak logout endpoint
        // window.location.href = '/login'; 
        // Better to just update state and navigate
    }

    isLoggedIn(): boolean {
        if (typeof localStorage !== 'undefined') {
            return !!localStorage.getItem('access_token');
        }
        return false;
    }
}
