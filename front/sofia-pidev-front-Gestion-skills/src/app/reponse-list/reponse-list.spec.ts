import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReponseListComponent } from './reponse-list';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('ReponseListComponent', () => {
  let component: ReponseListComponent;
  let fixture: ComponentFixture<ReponseListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReponseListComponent,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReponseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
