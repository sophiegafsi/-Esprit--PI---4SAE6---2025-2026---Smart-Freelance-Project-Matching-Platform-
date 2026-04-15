import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Availability } from '../models/availability.model';

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {
  private apiUrl = '/api/availabilities';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Availability[]> {
    return this.http.get<Availability[]>(this.apiUrl);
  }

  getById(id: number): Observable<Availability> {
    return this.http.get<Availability>(`${this.apiUrl}/${id}`);
  }

  getByDate(date: string): Observable<Availability[]> {
    return this.http.get<Availability[]>(`${this.apiUrl}/date/${date}`);
  }

  getByDateRange(start: string, end: string): Observable<Availability[]> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.http.get<Availability[]>(`${this.apiUrl}/date-range`, { params });
  }

  search(resourceName: string): Observable<Availability[]> {
    const params = new HttpParams().set('resourceName', resourceName);
    return this.http.get<Availability[]>(`${this.apiUrl}/search`, { params });
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
