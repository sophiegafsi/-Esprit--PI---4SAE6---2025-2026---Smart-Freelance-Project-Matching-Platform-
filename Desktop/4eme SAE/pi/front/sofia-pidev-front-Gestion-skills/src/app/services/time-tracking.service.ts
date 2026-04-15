import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TimeTrackingService {
  private apiUrl = 'http://localhost:8081/time-tracking/api/time-tracking'; // Routes through API Gateway

  public showTracker$ = new BehaviorSubject<boolean>(false);
  public activeContractId: string | null = null;
  public activeFreelancerId: string | null = null;

  constructor(private http: HttpClient) { }

  launchTracker(contractId: string, freelancerId: string) {
    this.activeContractId = contractId;
    this.activeFreelancerId = freelancerId;
    this.showTracker$.next(true);
  }

  closeTracker() {
    this.showTracker$.next(false);
  }

  startSession(contractId: string, freelancerId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/start`, { contractId, freelancerId });
  }

  stopSession(sessionId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${sessionId}/stop`, {});
  }

  addSnapshot(sessionId: string, screenshotUrl: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${sessionId}/snapshot`, { screenshotUrl });
  }

  getSessionsByContract(contractId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/contract/${contractId}`);
  }

  updateSessionStatus(sessionId: string, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${sessionId}/status?status=${status}`, {});
  }
}
