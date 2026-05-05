import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddPlanning } from './add-planning';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('AddPlanning', () => {
  let component: AddPlanning;
  let fixture: ComponentFixture<AddPlanning>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AddPlanning,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
