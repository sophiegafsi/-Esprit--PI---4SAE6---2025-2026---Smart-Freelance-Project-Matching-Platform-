import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SkillsService } from './skills.service';
import { Skill } from '../models/skill.model';

describe('SkillsService', () => {
  let service: SkillsService;
  let httpMock: HttpTestingController;

  const BASE_URL = 'http://localhost:8081/skills';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SkillsService]
    });
    service = TestBed.inject(SkillsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('search() should normalize various response formats into PageResponse', () => {
    const mockRes = {
      content: [
        { id: 1, name: 'Java', level: 'EXPERT', yearsOfExperience: 5 }
      ],
      totalPages: 1,
      totalElements: 1
    };

    service.search('Java', 0, 10, 'id', 'asc').subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].name).toBe('Java');
      expect(res.totalPages).toBe(1);
    });

    const req = httpMock.expectOne(request => request.url.includes('/search'));
    req.flush(mockRes);
  });

  it('search() should fallback to base URL if /search endpoint fails (trySequential)', () => {
    const mockRes = {
      content: [{ id: 2, name: 'Angular' }],
      totalPages: 1
    };

    service.search('Angular', 0, 10, 'id', 'asc').subscribe(res => {
      expect(res.content[0].name).toBe('Angular');
    });

    // First request to /search fails
    const req1 = httpMock.expectOne(request => request.url.endsWith('/search'));
    req1.flush('Not Found', { status: 404, statusText: 'Not Found' });

    // Second request to base URL should be triggered automatically
    const req2 = httpMock.expectOne(request => request.url === BASE_URL);
    expect(req2.request.method).toBe('GET');
    req2.flush(mockRes);
  });

  it('create() should handle raw text response if JSON parsing fails', () => {
    const newSkill: Skill = { name: 'Python', level: 'BEGINNER', yearsOfExperience: 1 };
    const textResponse = '{"id": 42, "name": "Python", "level": "BEGINNER", "yearsOfExperience": 1, "description": ""}';

    service.create(newSkill).subscribe(res => {
      expect(res.id).toBe(42);
      expect(res.name).toBe('Python');
    });

    // First attempt to /add fails with error to trigger fallback
    const req1 = httpMock.expectOne(`${BASE_URL}/add`);
    req1.flush('Server Error', { status: 500, statusText: 'Error' });

    // Second attempt (fallback) to /add but with responseType 'text'
    const req2 = httpMock.expectOne(`${BASE_URL}/add`);
    req2.flush(textResponse);
  });

  it('getScoreboard() should return an array of scores', () => {
    const mockScores = [{ userName: 'user1', score: 100 }, { userName: 'user2', score: 80 }];

    service.getScoreboard(5).subscribe(res => {
      expect(res.length).toBe(2);
      expect(res[0].userName).toBe('user1');
    });

    const req = httpMock.expectOne(`${BASE_URL}/scoreboard?size=5`);
    req.flush(mockScores);
  });
});
