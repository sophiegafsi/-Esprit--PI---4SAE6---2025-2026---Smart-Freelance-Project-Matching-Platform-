import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Availability } from '../models/availability.model';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {
    private apiUrl = 'http://localhost:8088/api/availabilities';

    constructor(private http: HttpClient) { }

    getAll(): Observable<Availability[]> {
        return this.http.get<Availability[]>(this.apiUrl);
    }

    getById(id: number): Observable<Availability> {
        return this.http.get<Availability>(`${this.apiUrl}/${id}`);
    }

    getByFreelancerId(id: string): Observable<Availability[]> {
        return this.http.get<Availability[]>(`${this.apiUrl}/freelancer/${id}`);
    }

    getMyAvailabilities(): Observable<Availability[]> {
        return this.http.get<Availability[]>(`${this.apiUrl}/my`);
    }

    getByDate(date: string): Observable<Availability[]> {
        return this.http.get<Availability[]>(`${this.apiUrl}/date/${date}`);
    }

    create(availability: Availability): Observable<Availability> {
        return this.http.post<Availability>(this.apiUrl, availability);
    }

    update(id: number, availability: Availability): Observable<Availability> {
        return this.http.put<Availability>(`${this.apiUrl}/${id}`, availability);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
