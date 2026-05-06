import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateProjetComponent } from './update-projet';

describe('UpdateProjetComponent', () => {
  let component: UpdateProjetComponent;
  let fixture: ComponentFixture<UpdateProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateProjetComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateProjetComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
