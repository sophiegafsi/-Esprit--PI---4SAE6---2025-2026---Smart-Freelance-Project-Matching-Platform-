import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReclamationFormComponent } from './reclamation-form';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

describe('ReclamationFormComponent', () => {
  let component: ReclamationFormComponent;
  let fixture: ComponentFixture<ReclamationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReclamationFormComponent,
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReclamationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
