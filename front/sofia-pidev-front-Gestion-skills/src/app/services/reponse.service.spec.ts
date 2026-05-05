import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReponseService } from './reponse.service';

describe('ReponseService', () => {
  let service: ReponseService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReponseService]
    });
    service = TestBed.inject(ReponseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
