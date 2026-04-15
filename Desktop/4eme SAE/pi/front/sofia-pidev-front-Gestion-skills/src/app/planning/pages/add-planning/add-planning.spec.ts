import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddPlanning } from './add-planning';

describe('AddPlanning', () => {
  let component: AddPlanning;
  let fixture: ComponentFixture<AddPlanning>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddPlanning]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddPlanning);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
