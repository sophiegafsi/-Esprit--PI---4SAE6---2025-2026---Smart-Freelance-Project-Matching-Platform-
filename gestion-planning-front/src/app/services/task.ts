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

  getTasksByPlanningId(planningId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/planning/${planningId}`);
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