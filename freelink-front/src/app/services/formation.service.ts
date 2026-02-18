import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// ✅ Interface Formation exportée (indispensable)
export interface Formation {
  id?: number;
  titre: string;
  description: string;
  status?: string;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class FormationService {

  // ✅ URL directe vers ton API (localhost:8083 fonctionne)
  private apiUrl = 'http://localhost:8083/api/formations';

  constructor(private http: HttpClient) {}

  // ✅ GET toutes les formations (pour formations.ts)
  getAll(): Observable<Formation[]> {
    return this.http.get<Formation[]>(this.apiUrl);
  }

  // ✅ POST créer une formation (pour add-training.ts)
  create(payload: Partial<Formation>): Observable<Formation> {
    return this.http.post<Formation>(this.apiUrl, payload);
  }
}
