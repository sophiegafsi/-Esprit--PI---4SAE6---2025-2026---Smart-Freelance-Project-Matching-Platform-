import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluationListPageComponent } from './evaluation-list-page.component';

describe('EvaluationListPageComponent', () => {
  let component: EvaluationListPageComponent;
  let fixture: ComponentFixture<EvaluationListPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EvaluationListPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluationListPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
