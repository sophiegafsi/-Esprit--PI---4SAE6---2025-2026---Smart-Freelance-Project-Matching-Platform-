import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReponseListComponent } from './reponse-list';

describe('ReponseListComponent', () => {
  let component: ReponseListComponent;
  let fixture: ComponentFixture<ReponseListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReponseListComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ReponseListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
