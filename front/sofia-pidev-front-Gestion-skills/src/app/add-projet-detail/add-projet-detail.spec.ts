import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddProjetDetailComponent } from './add-projet-detail';  // ← Corrigez le nom ici

describe('AddProjetDetailComponent', () => {  // ← Corrigez aussi ici
  let component: AddProjetDetailComponent;    // ← Et ici
  let fixture: ComponentFixture<AddProjetDetailComponent>;  // ← Et ici

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddProjetDetailComponent]  // ← Et ici
    })
      .compileComponents();

    fixture = TestBed.createComponent(AddProjetDetailComponent);  // ← Et ici
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
