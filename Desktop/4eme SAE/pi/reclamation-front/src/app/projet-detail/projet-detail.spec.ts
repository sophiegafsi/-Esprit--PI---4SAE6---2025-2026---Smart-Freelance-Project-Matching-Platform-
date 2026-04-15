import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjetDetailComponent } from './projet-detail';  // ← Corrigez le nom ici

describe('ProjetDetailComponent', () => {  // ← Corrigez ici
  let component: ProjetDetailComponent;    // ← Et ici
  let fixture: ComponentFixture<ProjetDetailComponent>;  // ← Et ici

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjetDetailComponent]  // ← Et ici
    })
      .compileComponents();

    fixture = TestBed.createComponent(ProjetDetailComponent);  // ← Et ici
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
