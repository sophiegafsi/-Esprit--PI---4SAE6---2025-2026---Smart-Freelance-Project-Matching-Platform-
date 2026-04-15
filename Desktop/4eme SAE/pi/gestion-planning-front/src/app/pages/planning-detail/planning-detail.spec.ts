import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanningDetail } from './planning-detail';

describe('PlanningDetail', () => {
  let component: PlanningDetail;
  let fixture: ComponentFixture<PlanningDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanningDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanningDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
