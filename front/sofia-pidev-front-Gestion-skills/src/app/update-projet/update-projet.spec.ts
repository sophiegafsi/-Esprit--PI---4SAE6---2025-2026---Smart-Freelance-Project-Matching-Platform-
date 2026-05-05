import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateProjet } from './update-projet';

describe('UpdateProjet', () => {
  let component: UpdateProjet;
  let fixture: ComponentFixture<UpdateProjet>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateProjet]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateProjet);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
