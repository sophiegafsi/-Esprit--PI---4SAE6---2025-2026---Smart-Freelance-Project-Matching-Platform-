import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostulerProjetComponent } from './postuler-projet';  // ← Corrigez le nom ici

describe('PostulerProjetComponent', () => {  // ← Corrigez ici
  let component: PostulerProjetComponent;    // ← Et ici
  let fixture: ComponentFixture<PostulerProjetComponent>;  // ← Et ici

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostulerProjetComponent]  // ← Et ici
    })
      .compileComponents();

    fixture = TestBed.createComponent(PostulerProjetComponent);  // ← Et ici
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
