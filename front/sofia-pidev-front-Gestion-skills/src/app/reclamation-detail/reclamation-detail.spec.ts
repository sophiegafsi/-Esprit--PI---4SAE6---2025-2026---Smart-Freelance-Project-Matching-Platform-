import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReclamationDetailComponent } from './reclamation-detail';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

describe('ReclamationDetailComponent', () => {
  let component: ReclamationDetailComponent;
  let fixture: ComponentFixture<ReclamationDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReclamationDetailComponent,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '1' }),
            paramMap: of(convertToParamMap({ id: '1' })),
            snapshot: { paramMap: convertToParamMap({ id: '1' }) }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReclamationDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
