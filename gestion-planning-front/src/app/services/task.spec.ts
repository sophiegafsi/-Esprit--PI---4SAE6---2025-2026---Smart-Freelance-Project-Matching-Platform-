import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TaskService } from './task';
import { Task } from '../models/task.model';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaskService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load tasks for one planning', () => {
    const tasks: Task[] = [
      {
        id: 1,
        title: 'Analysis',
        description: 'Prepare specifications',
        taskDate: '2026-04-20',
        startTime: '08:00',
        endTime: '09:00',
        priority: 'HIGH',
        status: 'TODO'
      }
    ];

    service.getTasksByPlanningId(9).subscribe((result) => {
      expect(result).toEqual(tasks);
    });

    const request = httpMock.expectOne('http://localhost:8086/api/tasks/planning/9');
    expect(request.request.method).toBe('GET');
    request.flush(tasks);
  });

  it('should search tasks by planning and keyword', () => {
    service.searchTasksByPlanning(3, 'high').subscribe();

    const request = httpMock.expectOne('http://localhost:8086/api/tasks/planning/3/search?keyword=high');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should add a task to a planning', () => {
    const task: Task = {
      title: 'Development',
      description: 'Implement feature',
      taskDate: '2026-04-21',
      startTime: '10:00',
      endTime: '12:00',
      priority: 'MEDIUM',
      status: 'IN_PROGRESS'
    };

    service.addTaskToPlanning(7, task).subscribe((created) => {
      expect(created.title).toBe('Development');
    });

    const request = httpMock.expectOne('http://localhost:8086/api/tasks/planning/7');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(task);
    request.flush({ id: 10, ...task });
  });

  it('should update a task', () => {
    const task: Task = {
      id: 5,
      title: 'Testing',
      description: 'Write unit tests',
      taskDate: '2026-04-22',
      startTime: '13:00',
      endTime: '15:00',
      priority: 'HIGH',
      status: 'DONE'
    };

    service.updateTask(5, task).subscribe((updated) => {
      expect(updated.status).toBe('DONE');
    });

    const request = httpMock.expectOne('http://localhost:8086/api/tasks/5');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(task);
    request.flush(task);
  });

  it('should delete a task', () => {
    service.deleteTask(11).subscribe();

    const request = httpMock.expectOne('http://localhost:8086/api/tasks/11');
    expect(request.request.method).toBe('DELETE');
    request.flush({});
  });
});
