import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReponseFormComponent } from './reponse-form';

describe('ReponseFormComponent', () => {
  let component: ReponseFormComponent;
  let fixture: ComponentFixture<ReponseFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReponseFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReponseFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
