import { ComponentFixture, TestBed } from '@angular/core/testing';
import { convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { EditPlanning } from './edit-planning';
import { PlanningService } from '../../services/planning';
import { PopupService } from '../../services/popup.service';

describe('EditPlanning', () => {
  let component: EditPlanning;
  let fixture: ComponentFixture<EditPlanning>;
  let planningService: {
    getPlanningById: ReturnType<typeof vi.fn>;
    updatePlanning: ReturnType<typeof vi.fn>;
  };
  let popupService: {
    close: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
    show: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(async () => {
    planningService = {
      getPlanningById: vi.fn().mockReturnValue(
        of({
          id: 5,
          title: 'Sprint 5',
          description: 'Planning to edit',
          startDate: '2026-04-20',
          endDate: '2026-04-25',
          status: 'ACTIVE'
        })
      ),
      updatePlanning: vi.fn().mockReturnValue(of({}))
    };

    popupService = {
      close: vi.fn(),
      error: vi.fn(),
      show: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [EditPlanning],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '5' })
            }
          }
        },
        { provide: PlanningService, useValue: planningService },
        { provide: PopupService, useValue: popupService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(EditPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load planning data on init', () => {
    expect(planningService.getPlanningById).toHaveBeenCalledWith(5);
    expect(component.planning.title).toBe('Sprint 5');
    expect(component.planning.status).toBe('ACTIVE');
  });

  it('should redirect to list when loading fails', () => {
    planningService.getPlanningById.mockReturnValueOnce(throwError(() => new Error('not found')));

    fixture = TestBed.createComponent(EditPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(popupService.error).toHaveBeenCalledWith('Error', 'Planning not found.');
    expect(router.navigate).toHaveBeenCalledWith(['/plannings']);
  });

  it('should update a planning and navigate after popup confirmation', () => {
    component.updatePlanning();

    expect(popupService.close).toHaveBeenCalled();
    expect(planningService.updatePlanning).toHaveBeenCalledWith(5, component.planning);
    expect(popupService.show).toHaveBeenCalledTimes(1);

    const popupOptions = popupService.show.mock.calls[0][3];
    popupOptions.onConfirm();

    expect(router.navigate).toHaveBeenCalledWith(['/plannings']);
  });

  it('should format backend error messages on update failure', () => {
    planningService.updatePlanning.mockReturnValue(
      throwError(() => ({
        error: {
          message: 'End date cannot be before start date'
        }
      }))
    );

    component.updatePlanning();

    expect(popupService.error).toHaveBeenCalledWith(
      'Validation Error',
      'End date cannot be before start date'
    );
  });
});
