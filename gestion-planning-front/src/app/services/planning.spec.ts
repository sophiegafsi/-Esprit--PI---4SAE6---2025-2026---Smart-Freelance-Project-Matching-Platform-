import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PlanningService } from './planning';
import { Planning } from '../models/planning.model';

describe('PlanningService', () => {
  let service: PlanningService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PlanningService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(PlanningService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load all plannings', () => {
    const expectedPlannings: Planning[] = [
      {
        id: 1,
        title: 'Sprint planning',
        description: 'Main planning',
        startDate: '2026-04-20',
        endDate: '2026-04-25',
        status: 'ACTIVE'
      }
    ];

    service.getAllPlannings().subscribe((plannings) => {
      expect(plannings).toEqual(expectedPlannings);
    });

    const request = httpMock.expectOne('http://localhost:8086/api/plannings');
    expect(request.request.method).toBe('GET');
    request.flush(expectedPlannings);
  });

  it('should search plannings by keyword', () => {
    service.searchPlannings('active').subscribe();

    const request = httpMock.expectOne('http://localhost:8086/api/plannings/search?keyword=active');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should create a planning', () => {
    const planning: Planning = {
      title: 'Release plan',
      description: 'Delivery schedule',
      startDate: '2026-04-20',
      endDate: '2026-04-28',
      status: 'ACTIVE'
    };

    service.addPlanning(planning).subscribe((created) => {
      expect(created.title).toBe('Release plan');
    });

    const request = httpMock.expectOne('http://localhost:8086/api/plannings');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(planning);
    request.flush({ id: 2, ...planning });
  });

  it('should update a planning', () => {
    const planning: Planning = {
      id: 4,
      title: 'Updated plan',
      description: 'Updated description',
      startDate: '2026-04-22',
      endDate: '2026-04-29',
      status: 'COMPLETED'
    };

    service.updatePlanning(4, planning).subscribe((updated) => {
      expect(updated.status).toBe('COMPLETED');
    });

    const request = httpMock.expectOne('http://localhost:8086/api/plannings/4');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(planning);
    request.flush(planning);
  });

  it('should load weighted progress metrics', () => {
    service.getPlanningWeightedProgress(6).subscribe((metrics) => {
      expect(metrics.weightedProgress).toBe(80);
    });

    const request = httpMock.expectOne('http://localhost:8086/api/plannings/6/weighted-progress');
    expect(request.request.method).toBe('GET');
    request.flush({
      planningId: 6,
      totalTasks: 3,
      weightedProgress: 80
    });
  });
});
