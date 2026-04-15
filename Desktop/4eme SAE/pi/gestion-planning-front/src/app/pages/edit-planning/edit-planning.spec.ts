import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditPlanning } from './edit-planning';

describe('EditPlanning', () => {
  let component: EditPlanning;
  let fixture: ComponentFixture<EditPlanning>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditPlanning]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditPlanning);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
