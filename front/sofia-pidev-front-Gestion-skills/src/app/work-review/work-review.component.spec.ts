import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkReviewComponent } from './work-review.component';

describe('WorkReviewComponent', () => {
  let component: WorkReviewComponent;
  let fixture: ComponentFixture<WorkReviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WorkReviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
