import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking } from '../models/booking.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private apiUrl = '/api/bookings';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.apiUrl);
  }

  getById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/${id}`);
  }

  getByUser(userId: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/user/${userId}`);
  }

  getByFreelancer(freelancerName: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/freelancer/${freelancerName}`);
  }

  getByAvailability(availabilityId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/availability/${availabilityId}`);
  }

  getAvailableSlots(availabilityId: number): Observable<{ availableSlots: number }> {
    return this.http.get<{ availableSlots: number }>(`${this.apiUrl}/availability/${availabilityId}/slots`);
  }

  create(booking: Booking): Observable<Booking> {
    return this.http.post<Booking>(this.apiUrl, booking);
  }

  cancel(id: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/${id}/cancel`, {});
  }

  confirm(id: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/${id}/confirm`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
