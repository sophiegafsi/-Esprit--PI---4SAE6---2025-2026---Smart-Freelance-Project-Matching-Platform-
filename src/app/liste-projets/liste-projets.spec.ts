import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ListeProjetsComponent } from './liste-projets';  // ← Corrigez le nom ici

describe('ListeProjetsComponent', () => {  // ← Corrigez ici
  let component: ListeProjetsComponent;    // ← Et ici
  let fixture: ComponentFixture<ListeProjetsComponent>;  // ← Et ici

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListeProjetsComponent]  // ← Et ici
    })
      .compileComponents();

    fixture = TestBed.createComponent(ListeProjetsComponent);  // ← Et ici
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
