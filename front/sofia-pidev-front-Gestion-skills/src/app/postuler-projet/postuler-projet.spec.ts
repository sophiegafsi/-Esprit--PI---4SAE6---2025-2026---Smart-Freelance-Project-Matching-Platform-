import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostulerProjetComponent } from './postuler-projet';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

describe('PostulerProjetComponent', () => {
  let component: PostulerProjetComponent;
  let fixture: ComponentFixture<PostulerProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PostulerProjetComponent,
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PostulerProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
