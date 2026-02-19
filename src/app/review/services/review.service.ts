import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Review {
  idR?: number;
  comment: string;
  score: number;
  evaluationId: number; // relie à l'évaluation
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private baseUrl = 'http://localhost:8081/review';

  constructor(private http: HttpClient) {}

  getAllReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/all`);
  }

  getReview(id: number): Observable<Review> {
    return this.http.get<Review>(`${this.baseUrl}/find?idR=${id}`);
  }

  addReview(review: Review): Observable<Review> {
    return this.http.post<Review>(`${this.baseUrl}/add`, review);
  }

  updateReview(review: Review): Observable<Review> {
    return this.http.put<Review>(`${this.baseUrl}/update`, review);
  }

  deleteReview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete?idR=${id}`);
  }
}
