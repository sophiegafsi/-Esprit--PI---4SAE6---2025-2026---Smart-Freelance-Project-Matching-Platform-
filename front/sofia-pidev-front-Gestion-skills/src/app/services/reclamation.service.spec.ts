import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReclamationService } from './reclamation.service';

describe('ReclamationService', () => {
  let service: ReclamationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReclamationService]
    });
    service = TestBed.inject(ReclamationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
