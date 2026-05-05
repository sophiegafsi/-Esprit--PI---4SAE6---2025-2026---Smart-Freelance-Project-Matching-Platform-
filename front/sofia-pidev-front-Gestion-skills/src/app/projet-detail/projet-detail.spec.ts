import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjetDetailComponent } from './projet-detail';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { DevisCalculatorService } from '../devis-calculator/devis-calculator.service';

describe('ProjetDetailComponent', () => {
  let component: ProjetDetailComponent;
  let fixture: ComponentFixture<ProjetDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProjetDetailComponent,
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
        },
        DevisCalculatorService
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjetDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
