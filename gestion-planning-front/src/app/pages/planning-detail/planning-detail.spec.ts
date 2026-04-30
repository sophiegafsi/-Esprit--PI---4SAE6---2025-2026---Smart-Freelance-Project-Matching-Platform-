import { ComponentFixture, TestBed } from '@angular/core/testing';
import { convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { PlanningDetail } from './planning-detail';
import { PlanningService } from '../../services/planning';
import { TaskService } from '../../services/task';
import { PopupService } from '../../services/popup.service';
import { Task } from '../../models/task.model';

describe('PlanningDetail', () => {
  let component: PlanningDetail;
  let fixture: ComponentFixture<PlanningDetail>;
  let planningService: {
    getPlanningById: ReturnType<typeof vi.fn>;
    getPlanningProgress: ReturnType<typeof vi.fn>;
    getPlanningWeightedProgress: ReturnType<typeof vi.fn>;
    getPlanningDailyLoad: ReturnType<typeof vi.fn>;
    getPlanningEfficiency: ReturnType<typeof vi.fn>;
  };
  let taskService: {
    getTasksByPlanningId: ReturnType<typeof vi.fn>;
    searchTasksByPlanning: ReturnType<typeof vi.fn>;
    addTaskToPlanning: ReturnType<typeof vi.fn>;
    updateTask: ReturnType<typeof vi.fn>;
    deleteTask: ReturnType<typeof vi.fn>;
  };
  let popupService: {
    close: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
    success: ReturnType<typeof vi.fn>;
    confirm: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    planningService = {
      getPlanningById: vi.fn().mockReturnValue(
        of({
          id: 1,
          title: 'Planning detail',
          description: 'Detailed planning',
          startDate: '2026-04-20',
          endDate: '2026-04-25',
          status: 'ACTIVE'
        })
      ),
      getPlanningProgress: vi.fn().mockReturnValue(
        of({
          planningId: 1,
          totalTasks: 2,
          doneTasks: 1,
          inProgressTasks: 1,
          todoTasks: 0,
          progress: 75
        })
      ),
      getPlanningWeightedProgress: vi.fn().mockReturnValue(
        of({
          planningId: 1,
          totalTasks: 2,
          weightedProgress: 80
        })
      ),
      getPlanningDailyLoad: vi.fn().mockReturnValue(
        of([
          {
            date: '2026-04-20',
            taskCount: 2,
            totalMinutes: 180,
            totalHours: 3
          }
        ])
      ),
      getPlanningEfficiency: vi.fn().mockReturnValue(
        of({
          planningId: 1,
          totalTasks: 2,
          wastedMinutes: 30,
          averageTaskDuration: 90,
          taskDensity: 0.86,
          efficiencyScore: 90,
          efficiencyLevel: 'HIGH'
        })
      )
    };

    taskService = {
      getTasksByPlanningId: vi.fn().mockReturnValue(
        of([
          {
            id: 10,
            title: 'Initial task',
            description: 'Task description',
            taskDate: '2026-04-20',
            startTime: '08:00',
            endTime: '09:00',
            priority: 'HIGH',
            status: 'TODO'
          }
        ])
      ),
      searchTasksByPlanning: vi.fn().mockReturnValue(of([])),
      addTaskToPlanning: vi.fn().mockReturnValue(of({})),
      updateTask: vi.fn().mockReturnValue(of({})),
      deleteTask: vi.fn().mockReturnValue(of({}))
    };

    popupService = {
      close: vi.fn(),
      error: vi.fn(),
      success: vi.fn(),
      confirm: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [PlanningDetail],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '1' })
            }
          }
        },
        { provide: PlanningService, useValue: planningService },
        { provide: TaskService, useValue: taskService },
        { provide: PopupService, useValue: popupService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PlanningDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load planning details, tasks and metrics on init', () => {
    expect(component.planning.title).toBe('Planning detail');
    expect(component.tasks.length).toBe(1);
    expect(component.progressData.progress).toBe(75);
    expect(component.weightedProgressData.weightedProgress).toBe(80);
    expect(component.efficiencyData.efficiencyLevel).toBe('HIGH');
  });

  it('should render loaded content', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Planning detail');
    expect(element.textContent).toContain('Initial task');
  });

  it('should add a task, reset the form and refresh dashboard data', () => {
    component.newTask = {
      title: 'New task',
      description: 'Create a new task',
      taskDate: '2026-04-21',
      startTime: '10:00',
      endTime: '11:00',
      priority: 'MEDIUM',
      status: 'TODO'
    };

    component.addTask();

    expect(popupService.close).toHaveBeenCalled();
    expect(taskService.addTaskToPlanning).toHaveBeenCalledWith(1, {
      title: 'New task',
      description: 'Create a new task',
      taskDate: '2026-04-21',
      startTime: '10:00',
      endTime: '11:00',
      priority: 'MEDIUM',
      status: 'TODO'
    });
    expect(component.newTask.title).toBe('');
    expect(component.newTask.status).toBe('TODO');
    expect(popupService.success).toHaveBeenCalledWith('Success', 'Task added successfully.');
  });

  it('should show backend validation errors when adding a task fails', () => {
    taskService.addTaskToPlanning.mockReturnValue(
      throwError(() => ({
        error: {
          messages: {
            title: 'Task title is required',
            endTime: 'End time must be after start time'
          }
        }
      }))
    );

    component.addTask();

    expect(popupService.error).toHaveBeenCalledWith(
      'Validation Error',
      'Task title is required<br>End time must be after start time'
    );
  });

  it('should prepare edit state when selecting a task', () => {
    const task: Task = {
      id: 12,
      title: 'Editable task',
      description: 'Editable description',
      taskDate: '2026-04-22',
      startTime: '14:00',
      endTime: '15:30',
      priority: 'HIGH',
      status: 'IN_PROGRESS'
    };

    component.startEditTask(task);

    expect(component.editingTaskId).toBe(12);
    expect(component.editTaskData.title).toBe('Editable task');
    expect(popupService.close).toHaveBeenCalled();
  });

  it('should update the selected task', () => {
    component.editingTaskId = 10;
    component.editTaskData = {
      id: 10,
      title: 'Updated task',
      description: 'Updated description',
      taskDate: '2026-04-23',
      startTime: '15:00',
      endTime: '16:00',
      priority: 'LOW',
      status: 'DONE'
    };

    component.updateTask();

    expect(taskService.updateTask).toHaveBeenCalledWith(10, {
      id: 10,
      title: 'Updated task',
      description: 'Updated description',
      taskDate: '2026-04-23',
      startTime: '15:00',
      endTime: '16:00',
      priority: 'LOW',
      status: 'DONE'
    });
    expect(component.editingTaskId).toBeNull();
    expect(popupService.success).toHaveBeenCalledWith('Success', 'Task updated successfully.');
  });

  it('should delete a task after confirmation and clear edit mode if needed', () => {
    popupService.confirm.mockImplementation((_title, _message, onConfirm) => onConfirm());
    component.editingTaskId = 10;

    component.deleteTask(10);

    expect(taskService.deleteTask).toHaveBeenCalledWith(10);
    expect(component.editingTaskId).toBeNull();
    expect(popupService.success).toHaveBeenCalledWith('Deleted', 'Task deleted successfully.');
  });

  it('should search tasks inside the planning', () => {
    taskService.searchTasksByPlanning.mockReturnValue(
      of([
        {
          id: 30,
          title: 'Filtered task',
          description: 'Result',
          taskDate: '2026-04-22',
          startTime: '16:00',
          endTime: '17:00',
          priority: 'HIGH',
          status: 'DONE'
        }
      ])
    );

    component.taskSearchKeyword = 'high';
    component.onTaskSearchChange();

    expect(taskService.searchTasksByPlanning).toHaveBeenCalledWith(1, 'high');
    expect(component.tasks[0].title).toBe('Filtered task');
  });
});
