import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReclamationListComponent } from './reclamation-list';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('ReclamationListComponent', () => {
  let component: ReclamationListComponent;
  let fixture: ComponentFixture<ReclamationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReclamationListComponent,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReclamationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
