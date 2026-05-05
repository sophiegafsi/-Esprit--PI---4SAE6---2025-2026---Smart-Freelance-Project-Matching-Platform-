import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PlanningDetail } from './planning-detail';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('PlanningDetail', () => {
  let component: PlanningDetail;
  let fixture: ComponentFixture<PlanningDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PlanningDetail,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '1' } },
            params: of({ id: '1' })
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanningDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
