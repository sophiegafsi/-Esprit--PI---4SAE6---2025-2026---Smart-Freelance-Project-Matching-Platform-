import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
    private apiUrl = 'http://localhost:8088/api/bookings';

    constructor(private http: HttpClient) { }

    create(booking: Booking): Observable<Booking> {
        return this.http.post<Booking>(this.apiUrl, booking);
    }

    getByUser(userId: string): Observable<Booking[]> {
        return this.http.get<Booking[]>(`${this.apiUrl}/user/${userId}`);
    }

    getByAvailability(availabilityId: number): Observable<Booking[]> {
        return this.http.get<Booking[]>(`${this.apiUrl}/availability/${availabilityId}`);
    }

    getMyBookingsAsFreelancer(): Observable<Booking[]> {
        return this.http.get<Booking[]>(`${this.apiUrl}/freelancer`);
    }

    getMyBookingsAsClient(userId: string): Observable<Booking[]> {
        return this.http.get<Booking[]>(`${this.apiUrl}/user/${userId}`);
    }

    cancel(id: number): Observable<Booking> {
        return this.http.put<Booking>(`${this.apiUrl}/${id}/cancel`, {});
    }

    confirm(id: number): Observable<Booking> {
        return this.http.put<Booking>(`${this.apiUrl}/${id}/confirm`, {});
    }
}
