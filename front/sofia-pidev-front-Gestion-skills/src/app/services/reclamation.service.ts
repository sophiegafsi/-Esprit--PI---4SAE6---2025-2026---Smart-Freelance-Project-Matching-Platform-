import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reclamation } from '../models/reclamation.model';

export interface DuplicateCheckResponse {
  idReclamation: number;
  sujet: string;
  description: string;
  similarityScore: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReclamationService {
  private apiUrl = 'http://localhost:8085/api/reclamations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.apiUrl}/list`);
  }

  getById(id: number): Observable<Reclamation> {
    return this.http.get<Reclamation>(`${this.apiUrl}/get/${id}`);
  }

  create(reclamation: Reclamation): Observable<Reclamation> {
    return this.http.post<Reclamation>(`${this.apiUrl}/addreclamation`, reclamation);
  }

  update(id: number, reclamation: Reclamation): Observable<Reclamation> {
    return this.http.put<Reclamation>(`${this.apiUrl}/update/${id}`, reclamation);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }

  search(filters: {
    search?: string;
    type?: string;
    priorite?: string;
    statut?: string;
  }): Observable<Reclamation[]> {
    let params = new HttpParams();

    if (filters.search?.trim()) {
      params = params.set('search', filters.search.trim());
    }

    if (filters.type?.trim()) {
      params = params.set('type', filters.type);
    }

    if (filters.priorite?.trim()) {
      params = params.set('priorite', filters.priorite);
    }

    if (filters.statut?.trim()) {
      params = params.set('statut', filters.statut);
    }

    return this.http.get<Reclamation[]>(`${this.apiUrl}/search`, { params });
  }

  checkDuplicates(data: { sujet: string; description: string }): Observable<DuplicateCheckResponse[]> {
    return this.http.post<DuplicateCheckResponse[]>(`${this.apiUrl}/check-duplicates`, data);
  }
}
