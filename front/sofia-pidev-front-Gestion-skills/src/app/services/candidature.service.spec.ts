import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CandidatureService } from './candidature.service';
import { Candidature, CandidatureStatus } from '../models/candidature.model';

describe('CandidatureService', () => {
  let service: CandidatureService;
  let httpMock: HttpTestingController;

  const API_URL = 'http://localhost:8081/condidature/api/candidatures';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidatureService],
    });

    service = TestBed.inject(CandidatureService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMyApplications() should GET candidatures for a given freelancer ID', () => {
    const freelancerId = 'freelancer-uuid-001';
    const mockCandidatures: Candidature[] = [
      { id: 'c1', freelancerId, projectId: 'p1', coverLetter: 'My letter', status: CandidatureStatus.PENDING } as any,
    ];

    service.getMyApplications(freelancerId).subscribe((result) => {
      expect(result.length).toBe(1);
      expect(result[0].freelancerId).toBe(freelancerId);
      expect(result[0].status).toBe(CandidatureStatus.PENDING);
    });

    const req = httpMock.expectOne(`${API_URL}/my?freelancerId=${freelancerId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCandidatures);
  });

  it('acceptApplication() should PUT to the accept endpoint', () => {
    const candidatureId = 'c-001';
    const clientId = 'client-uuid-001';
    const mockResponse: Partial<Candidature> = { id: candidatureId, status: CandidatureStatus.ACCEPTED };

    service.acceptApplication(candidatureId, clientId).subscribe((res) => {
      expect(res.status).toBe(CandidatureStatus.ACCEPTED);
    });

    const req = httpMock.expectOne(
      `${API_URL}/${candidatureId}/accept?clientId=${clientId}`
    );
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('rejectApplication() should PUT to the reject endpoint', () => {
    const candidatureId = 'c-002';
    const clientId = 'client-uuid-002';
    const mockResponse: Partial<Candidature> = { id: candidatureId, status: CandidatureStatus.REJECTED };

    service.rejectApplication(candidatureId, clientId).subscribe((res) => {
      expect(res.status).toBe(CandidatureStatus.REJECTED);
    });

    const req = httpMock.expectOne(
      `${API_URL}/${candidatureId}/reject?clientId=${clientId}`
    );
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('deleteApplication() should DELETE the candidature', () => {
    const candidatureId = 'c-003';
    const freelancerId = 'freelancer-uuid-003';

    service.deleteApplication(candidatureId, freelancerId).subscribe((res) => {
      // Changed from toBeUndefined() to toBeNull() to match req.flush(null)
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne(
      `${API_URL}/${candidatureId}?freelancerId=${freelancerId}`
    );
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
