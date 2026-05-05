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
  private apiUrl = 'http://localhost:8085/api/reclamations';
  private moderationUrl = 'http://localhost:8085/api/reponses';
  private sentimentUrl = 'http://localhost:8085/api/sentiment';

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
