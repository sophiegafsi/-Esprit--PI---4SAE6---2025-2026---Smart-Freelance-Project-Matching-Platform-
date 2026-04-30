import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { ListPlanning } from './list-planning';
import { PlanningService } from '../../services/planning';
import { PopupService } from '../../services/popup.service';

describe('ListPlanning', () => {
  let component: ListPlanning;
  let fixture: ComponentFixture<ListPlanning>;
  let planningService: {
    getAllPlannings: ReturnType<typeof vi.fn>;
    getPlanningWeightedProgress: ReturnType<typeof vi.fn>;
    getPlanningEfficiency: ReturnType<typeof vi.fn>;
    searchPlannings: ReturnType<typeof vi.fn>;
    deletePlanning: ReturnType<typeof vi.fn>;
  };
  let popupService: {
    error: ReturnType<typeof vi.fn>;
    success: ReturnType<typeof vi.fn>;
    confirm: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    planningService = {
      getAllPlannings: vi.fn().mockReturnValue(
        of([
          {
            id: 1,
            title: 'Planning A',
            description: 'Description A',
            startDate: '2026-04-20',
            endDate: '2026-04-25',
            status: 'ACTIVE'
          }
        ])
      ),
      getPlanningWeightedProgress: vi.fn().mockReturnValue(
        of({
          planningId: 1,
          totalTasks: 3,
          weightedProgress: 80
        })
      ),
      getPlanningEfficiency: vi.fn().mockReturnValue(
        of({
          planningId: 1,
          totalTasks: 3,
          wastedMinutes: 60,
          averageTaskDuration: 60,
          taskDensity: 0.75,
          efficiencyScore: 81.5,
          efficiencyLevel: 'HIGH'
        })
      ),
      searchPlannings: vi.fn().mockReturnValue(of([])),
      deletePlanning: vi.fn().mockReturnValue(of({}))
    };

    popupService = {
      error: vi.fn(),
      success: vi.fn(),
      confirm: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ListPlanning],
      providers: [
        provideRouter([]),
        { provide: PlanningService, useValue: planningService },
        { provide: PopupService, useValue: popupService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ListPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load plannings and extra metrics on init', () => {
    expect(component.plannings.length).toBe(1);
    expect(component.weightedProgressMap[1]).toBe(80);
    expect(component.efficiencyMap[1].efficiencyLevel).toBe('HIGH');
  });

  it('should render loaded planning title', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Planning A');
  });

  it('should search plannings when keyword is provided', () => {
    planningService.searchPlannings.mockReturnValue(
      of([
        {
          id: 2,
          title: 'Filtered planning',
          description: 'Search result',
          startDate: '2026-04-26',
          endDate: '2026-04-30',
          status: 'COMPLETED'
        }
      ])
    );
    planningService.getPlanningWeightedProgress.mockReturnValue(
      of({ planningId: 2, totalTasks: 2, weightedProgress: 100 })
    );
    planningService.getPlanningEfficiency.mockReturnValue(
      of({
        planningId: 2,
        totalTasks: 2,
        wastedMinutes: 0,
        averageTaskDuration: 90,
        taskDensity: 1,
        efficiencyScore: 100,
        efficiencyLevel: 'HIGH'
      })
    );

    component.searchKeyword = 'filtered';
    component.onSearchChange();

    expect(planningService.searchPlannings).toHaveBeenCalledWith('filtered');
    expect(component.plannings[0].title).toBe('Filtered planning');
    expect(component.weightedProgressMap[2]).toBe(100);
  });

  it('should reload all plannings when search is cleared', () => {
    const loadSpy = vi.spyOn(component, 'loadPlannings');

    component.searchKeyword = '   ';
    component.onSearchChange();

    expect(loadSpy).toHaveBeenCalled();
  });

  it('should delete a planning after confirmation', () => {
    popupService.confirm.mockImplementation((_title, _message, onConfirm) => onConfirm());

    component.deletePlanning(1);

    expect(planningService.deletePlanning).toHaveBeenCalledWith(1);
    expect(popupService.success).toHaveBeenCalledWith('Deleted', 'Planning deleted successfully.');
  });

  it('should show an error popup when loading plannings fails', () => {
    planningService.getAllPlannings.mockReturnValueOnce(throwError(() => new Error('boom')));

    fixture = TestBed.createComponent(ListPlanning);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(popupService.error).toHaveBeenCalledWith('Error', 'Unable to load plannings.');
  });
});
