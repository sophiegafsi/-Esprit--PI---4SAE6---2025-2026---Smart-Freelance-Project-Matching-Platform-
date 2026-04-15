import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8086/api/tasks';

  constructor(private http: HttpClient) {}

  getAllTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(this.apiUrl);
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  getTasksByPlanningId(planningId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/planning/${planningId}`);
  }

  searchTasks(keyword: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/search?keyword=${keyword}`);
  }

  searchTasksByPlanning(planningId: number, keyword: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/planning/${planningId}/search?keyword=${keyword}`);
  }

  addTaskToPlanning(planningId: number, task: Task): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/planning/${planningId}`, task);
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
  }

  deleteTask(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}