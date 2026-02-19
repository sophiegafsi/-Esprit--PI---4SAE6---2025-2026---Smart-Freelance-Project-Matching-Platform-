import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EvaluationPayload {
  score: number;
  comment: string;
  projectId: number;
  user_id: number;
  typeEvaluation: string;
}

export interface Evaluation extends EvaluationPayload {
  idE: number;
  date: string;
}

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {

  private baseUrl = 'http://localhost:8081/evaluation'; // Gateway URL

  constructor(private http: HttpClient) { }

  createEvaluation(payload: EvaluationPayload): Observable<Evaluation> {
    return this.http.post<Evaluation>(`${this.baseUrl}/add`, payload);
  }

  updateEvaluation(payload: Evaluation): Observable<Evaluation> {
    return this.http.put<Evaluation>(`${this.baseUrl}/update`, payload);
  }

  deleteEvaluation(idE: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete/${idE}`);
  }

  getEvaluations(): Observable<Evaluation[]> {
    return this.http.get<Evaluation[]>(`${this.baseUrl}/all`);
  }
}
