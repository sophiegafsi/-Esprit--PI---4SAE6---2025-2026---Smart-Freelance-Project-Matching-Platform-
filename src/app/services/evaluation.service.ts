import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Evaluation } from '../models/evaluation';

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {
  private apiUrl = 'http://localhost:8085/evaluations';

  constructor(private http: HttpClient) {}

  getEvaluations(): Observable<Evaluation[]> {
    return this.http.get<any[]>(`${this.apiUrl}/all`).pipe(
      map(data => data.map(item => ({
        id: item.idE,
        projectName: item.projectName,
        evaluatorName: item.evaluatorName,
        evaluatedUserName: item.evaluatedUserName,
        score: item.score,
        comment: item.comment,
        anonymous: item.anonymous,
        date: item.date,
        typeEvaluation: item.typeEvaluation
      })))
    );
  }

  getEvaluationById(id: number): Observable<Evaluation> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(item => ({
        id: item.idE,
        projectName: item.projectName,
        evaluatorName: item.evaluatorName,
        evaluatedUserName: item.evaluatedUserName,
        score: item.score,
        comment: item.comment,
        anonymous: item.anonymous,
        date: item.date,
        typeEvaluation: item.typeEvaluation
      }))
    );
  }

  createEvaluation(evaluation: Evaluation): Observable<Evaluation> {
    return this.http.post<Evaluation>(`${this.apiUrl}/add`, evaluation);
  }

  updateEvaluation(id: number, evaluation: Evaluation): Observable<Evaluation> {
    return this.http.put<Evaluation>(`${this.apiUrl}/update/${id}`, evaluation);
  }

  deleteEvaluation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}