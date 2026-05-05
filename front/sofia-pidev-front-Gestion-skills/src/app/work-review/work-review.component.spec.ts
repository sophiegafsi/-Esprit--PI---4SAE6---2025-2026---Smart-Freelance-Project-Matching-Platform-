import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WorkReviewComponent } from './work-review.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('WorkReviewComponent', () => {
  let component: WorkReviewComponent;
  let fixture: ComponentFixture<WorkReviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        WorkReviewComponent,
        HttpClientTestingModule
      ]
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
