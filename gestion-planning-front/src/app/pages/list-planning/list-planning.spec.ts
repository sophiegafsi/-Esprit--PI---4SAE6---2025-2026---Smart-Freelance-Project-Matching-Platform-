import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListPlanning } from './list-planning';

describe('ListPlanning', () => {
  let component: ListPlanning;
  let fixture: ComponentFixture<ListPlanning>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListPlanning]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListPlanning);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
