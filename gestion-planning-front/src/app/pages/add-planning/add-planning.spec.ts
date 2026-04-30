import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { provideRouter, Router } from '@angular/router';
import { AddPlanning } from './add-planning';
import { PlanningService } from '../../services/planning';
import { PopupService } from '../../services/popup.service';

describe('AddPlanning', () => {
  let component: AddPlanning;
  let fixture: ComponentFixture<AddPlanning>;
  let planningService: {
    addPlanning: ReturnType<typeof vi.fn>;
  };
  let popupService: {
    close: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
    show: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(async () => {
    planningService = {
      addPlanning: vi.fn()
    };

    popupService = {
      close: vi.fn(),
      error: vi.fn(),
      show: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [AddPlanning],
      providers: [
        provideRouter([]),
        { provide: PlanningService, useValue: planningService },
        { provide: PopupService, useValue: popupService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(AddPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render the page title', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('h1')?.textContent).toContain('Add New Planning');
  });

  it('should save a planning and navigate after popup confirmation', () => {
    planningService.addPlanning.mockReturnValue(of({ id: 1, ...component.planning }));

    component.savePlanning();

    expect(popupService.close).toHaveBeenCalled();
    expect(planningService.addPlanning).toHaveBeenCalledWith(component.planning);
    expect(popupService.show).toHaveBeenCalledTimes(1);

    const popupOptions = popupService.show.mock.calls[0][3];
    popupOptions.onConfirm();

    expect(router.navigate).toHaveBeenCalledWith(['/plannings']);
  });

  it('should show formatted backend validation errors', () => {
    planningService.addPlanning.mockReturnValue(
      throwError(() => ({
        error: {
          messages: {
            title: 'Title is required',
            endDate: 'End date is required'
          }
        }
      }))
    );

    component.savePlanning();

    expect(popupService.error).toHaveBeenCalledWith(
      'Validation Error',
      'Title is required<br>End date is required'
    );
  });
});
