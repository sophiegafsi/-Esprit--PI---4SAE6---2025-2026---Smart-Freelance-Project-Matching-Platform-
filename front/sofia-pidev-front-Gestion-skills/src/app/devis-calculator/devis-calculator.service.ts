// src/app/devis-calculator/devis-calculator.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, shareReplay, throwError } from 'rxjs';

export interface DevisResult {
  prixMinimum: number;
  prixRecommande: number;
  prixMaximum: number;

  heuresEstimees: number;
  tauxHoraire: number;
  facteurComplexite: number;
  facteurUrgence: number;
  joursDisponibles: number;

  confiance: number;
  recommandation: string;

  decomposition: Array<{
    poste: string;
    heures: number;
    montant: number;
    pourcentage: number;
  }>;
}

export interface DevisRequest {
  projetId: number;
  deadline?: string | null;
}

@Injectable({ providedIn: 'root' })
export class DevisCalculatorService {
  private readonly apiUrl = 'http://localhost:8081/projet';

  // ✅ Cache en mémoire (clé = projetId + deadline)
  private cache = new Map<string, Observable<DevisResult>>();

  constructor(private http: HttpClient) { }

  calculerDevisDepuisBackend(projetId: number, deadline?: string): Observable<DevisResult> {
    const normalized = deadline ? this.normalizeDateForApi(deadline) : '';
    const key = `${projetId}|${normalized}`;

    // ✅ Si déjà calculé (ou en cours), on réutilise le même Observable
    const cached = this.cache.get(key);
    if (cached) return cached;

    const payload: DevisRequest = { projetId, deadline: normalized || null };

    const req$ = this.http
      .post<DevisResult>(`${this.apiUrl}/api/devis/calculate`, payload)
      .pipe(
        // ✅ Partage le résultat entre plusieurs abonnements et garde en mémoire
        shareReplay({ bufferSize: 1, refCount: false }),
        catchError((err) => {
          // ❌ si erreur => on enlève du cache, sinon ça restera bloqué
          this.cache.delete(key);
          return this.handleError(err);
        })
      );

    this.cache.set(key, req$);
    return req$;
  }

  // Optionnel : vider le cache si tu changes beaucoup de données
  clearCache(): void {
    this.cache.clear();
  }

  private normalizeDateForApi(date: any): string {
    if (!date) return '';

    if (typeof date === 'string' && date.includes('T')) {
      return date.split('T')[0];
    }

    if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date;
    }

    if (typeof date === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(date)) {
      const [dd, mm, yyyy] = date.split('/');
      return `${yyyy}-${mm}-${dd}`;
    }

    try {
      const d = new Date(date);
      if (!isNaN(d.getTime())) return d.toISOString().split('T')[0];
    } catch { }

    return '';
  }

  private handleError(error: HttpErrorResponse) {
    console.error('❌ Erreur devis:', error);

    let msg = 'Erreur lors du calcul du devis.';

    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      msg = error.error;
    }

    if (error.error && typeof error.error === 'object') {
      msg = (error.error as any).message || msg;
    }

    return throwError(() => new Error(msg));
  }
}
