import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReponseForm } from './reponse-form';

describe('ReponseForm', () => {
  let component: ReponseForm;
  let fixture: ComponentFixture<ReponseForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReponseForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReponseForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
