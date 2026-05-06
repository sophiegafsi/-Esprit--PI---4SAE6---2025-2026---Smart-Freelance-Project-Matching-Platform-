import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reponse } from '../models/reclamation.model';

export interface ModerationResult {
  allowed: boolean;
  reason: string;
  suggestion: string;
}

export interface SentimentResult {
  sentiment: 'POSITIVE' | 'NEUTRE' | 'NEGATIVE';
  reason: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReponseService {
  private apiUrl = 'http://20.240.47.244:30081/evaluation/api/reclamations';
  private moderationUrl = 'http://20.240.47.244:30081/evaluation/api/reponses';
  private sentimentUrl = 'http://20.240.47.244:30081/evaluation/api/sentiment';

  constructor(private http: HttpClient) {}

  getReponsesByReclamation(reclamationId: number): Observable<Reponse[]> {
    return this.http.get<Reponse[]>(
      `${this.apiUrl}/${reclamationId}/reponses/list`
    );
  }

  addReponse(reclamationId: number, reponse: Reponse): Observable<Reponse> {
    return this.http.post<Reponse>(
      `${this.apiUrl}/${reclamationId}/reponses/add`,
      reponse
    );
  }

  updateReponse(
    reclamationId: number,
    reponseId: number,
    reponse: Reponse
  ): Observable<Reponse> {
    return this.http.put<Reponse>(
      `${this.apiUrl}/${reclamationId}/reponses/update/${reponseId}`,
      reponse
    );
  }

  deleteReponse(reclamationId: number, reponseId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${reclamationId}/reponses/delete/${reponseId}`
    );
  }

  moderateMessage(message: string): Observable<ModerationResult> {
    return this.http.post<ModerationResult>(
      `${this.moderationUrl}/moderate`,
      { message }
    );
  }

  analyzeSentiment(message: string): Observable<SentimentResult> {
    return this.http.post<SentimentResult>(
      `${this.sentimentUrl}/analyze`,
      { message }
    );
  }
}
