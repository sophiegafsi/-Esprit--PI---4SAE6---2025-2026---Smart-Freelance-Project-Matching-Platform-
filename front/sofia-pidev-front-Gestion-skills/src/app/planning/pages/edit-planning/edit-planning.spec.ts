import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditPlanning } from './edit-planning';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('EditPlanning', () => {
  let component: EditPlanning;
  let fixture: ComponentFixture<EditPlanning>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EditPlanning,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '1' } }
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
