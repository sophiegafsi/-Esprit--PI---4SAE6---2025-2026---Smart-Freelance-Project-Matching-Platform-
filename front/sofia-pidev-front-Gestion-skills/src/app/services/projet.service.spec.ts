import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProjetService } from './projet.service';
import { Projet } from '../models/projet';

describe('ProjetService', () => {
  let service: ProjetService;
  let httpMock: HttpTestingController;

  const API_URL = 'http://localhost:8081/projet';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProjetService]
    });
    service = TestBed.inject(ProjetService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getProjets() should return a list of projects via GET', () => {
    const mockProjets: Projet[] = [
      { id: 1, title: 'Project 1', description: 'Desc 1', date: '2024-01-01', domaine: 'WEB' },
      { id: 2, title: 'Project 2', description: 'Desc 2', date: '2024-02-01', domaine: 'MOBILE' }
    ];

    service.getProjets().subscribe(projets => {
      expect(projets.length).toBe(2);
      expect(projets).toEqual(mockProjets);
    });

    const req = httpMock.expectOne(`${API_URL}/api/projets/allprojets`);
    expect(req.request.method).toBe('GET');
    req.flush(mockProjets);
  });

  it('addProject() should send a POST request with project data', () => {
    const newProject: Projet = { title: 'New', description: 'Desc', date: '2024-05-05', domaine: 'IA' };
    const mockResponse: Projet = { id: 10, ...newProject };

    service.addProject(newProject).subscribe(res => {
      expect(res.id).toBe(10);
      expect(res.title).toBe('New');
    });

    const req = httpMock.expectOne(`${API_URL}/api/projets/addprojet`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newProject);
    req.flush(mockResponse);
  });

  it('calculateDevis() should POST to the devis endpoint', () => {
    const projectId = 5;
    const mockDevis = { total: 1500, currency: 'EUR' };

    service.calculateDevis(projectId, '2024-12-31').subscribe(res => {
      expect(res.total).toBe(1500);
    });

    const req = httpMock.expectOne(`${API_URL}/api/devis/calculate`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ projetId: projectId, deadline: '2024-12-31' });
    req.flush(mockDevis);
  });

  it('should handle API errors via catchError', () => {
    service.getProjets().subscribe({
      next: () => fail('Should have failed'),
      error: (error) => {
        expect(error.status).toBe(500);
      }
    });

    const req = httpMock.expectOne(`${API_URL}/api/projets/allprojets`);
    req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
  });
});
