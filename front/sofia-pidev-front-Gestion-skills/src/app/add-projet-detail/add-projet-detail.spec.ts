import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddProjetDetailComponent } from './add-projet-detail';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

describe('AddProjetDetailComponent', () => {
  let component: AddProjetDetailComponent;
  let fixture: ComponentFixture<AddProjetDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AddProjetDetailComponent,
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
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddProjetDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
