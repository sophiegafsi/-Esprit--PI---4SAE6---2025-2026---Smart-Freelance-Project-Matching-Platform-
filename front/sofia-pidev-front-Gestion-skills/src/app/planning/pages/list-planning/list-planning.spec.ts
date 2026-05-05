import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ListPlanning } from './list-planning';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('ListPlanning', () => {
  let component: ListPlanning;
  let fixture: ComponentFixture<ListPlanning>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ListPlanning,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
