import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormationContentComponent } from './formation-content';

describe('FormationContentComponent', () => {
  let component: FormationContentComponent;
  let fixture: ComponentFixture<FormationContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormationContentComponent], // standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(FormationContentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
