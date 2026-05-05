import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { Task } from '../../models/task.model';
import { PlanningProgress } from '../../models/planning-progress.model';
import { PlanningWeightedProgress } from '../../models/planning-weighted-progress.model';
import { PlanningDailyLoad } from '../../models/planning-daily-load.model';
import { PlanningEfficiency } from '../../models/planning-efficiency.model';
import { PlanningService } from '../../services/planning';
import { TaskService } from '../../services/task';
import { PopupService } from '../../services/popup.service';

@Component({
  selector: 'app-planning-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './planning-detail.html',
  styleUrl: './planning-detail.css'
})
export class PlanningDetail implements OnInit {
  planningId!: number;

  planning: Planning = {
    id: 0,
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    status: 'ACTIVE'
  };

  progressData: PlanningProgress = {
    planningId: 0,
    totalTasks: 0,
    doneTasks: 0,
    inProgressTasks: 0,
    todoTasks: 0,
    progress: 0
  };

  weightedProgressData: PlanningWeightedProgress = {
    planningId: 0,
    totalTasks: 0,
    weightedProgress: 0
  };

  dailyLoadData: PlanningDailyLoad[] = [];

  efficiencyData: PlanningEfficiency = {
    planningId: 0,
    totalTasks: 0,
    wastedMinutes: 0,
    averageTaskDuration: 0,
    taskDensity: 0,
    efficiencyScore: 0,
    efficiencyLevel: 'LOW'
  };

  tasks: Task[] = [];
  taskSearchKeyword: string = '';

  newTask: Task = {
    title: '',
    description: '',
    taskDate: '',
    startTime: '',
    endTime: '',
    priority: 'MEDIUM',
    status: 'TODO'
  };

  editingTaskId: number | null = null;

  editTaskData: Task = {
    id: 0,
    title: '',
    description: '',
    taskDate: '',
    startTime: '',
    endTime: '',
    priority: 'MEDIUM',
    status: 'TODO'
  };

  constructor(
    private route: ActivatedRoute,
    private planningService: PlanningService,
    private taskService: TaskService,
    private popupService: PopupService
  ) { }

  ngOnInit(): void {
    this.planningId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPlanning();
    this.loadTasks();
    this.loadProgress();
    this.loadWeightedProgress();
    this.loadDailyLoad();
    this.loadEfficiency();
  }

  loadPlanning(): void {
    this.planningService.getPlanningById(this.planningId).subscribe({
      next: (data: Planning) => {
        this.planning = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement planning', err);
        this.popupService.error('Error', 'Unable to load planning details.');
      }
    });
  }

  loadTasks(): void {
    this.taskService.getTasksByPlanningId(this.planningId).subscribe({
      next: (data: Task[]) => {
        this.tasks = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement tasks', err);
        this.popupService.error('Error', 'Unable to load tasks.');
      }
    });
  }

  onTaskSearchChange(): void {
    const keyword = this.taskSearchKeyword.trim();

    if (!keyword) {
      this.loadTasks();
      return;
    }

    this.taskService.searchTasksByPlanning(this.planningId, keyword).subscribe({
      next: (data: Task[]) => {
        this.tasks = data;
      },
      error: (err: any) => {
        console.error('Erreur recherche task', err);
      }
    });
  }

  clearTaskSearch(): void {
    this.taskSearchKeyword = '';
    this.loadTasks();
  }

  loadProgress(): void {
    this.planningService.getPlanningProgress(this.planningId).subscribe({
      next: (data: PlanningProgress) => {
        this.progressData = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement progression', err);
      }
    });
  }

  loadWeightedProgress(): void {
    this.planningService.getPlanningWeightedProgress(this.planningId).subscribe({
      next: (data: PlanningWeightedProgress) => {
        this.weightedProgressData = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement weighted progress', err);
      }
    });
  }

  loadDailyLoad(): void {
    this.planningService.getPlanningDailyLoad(this.planningId).subscribe({
      next: (data: PlanningDailyLoad[]) => {
        this.dailyLoadData = data;
      },
      error: (err: any) => {
        console.error('Erreur daily load', err);
      }
    });
  }

  loadEfficiency(): void {
    this.planningService.getPlanningEfficiency(this.planningId).subscribe({
      next: (data: PlanningEfficiency) => {
        this.efficiencyData = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement efficiency', err);
      }
    });
  }

  private formatBackendError(err: any): string {
    console.log('FULL ERROR BODY:', err?.error);

    const errorBody = err?.error;

    // 1. Handle validation errors (Map<String, String> under 'messages')
    if (errorBody?.messages && typeof errorBody.messages === 'object') {
      return Object.entries(errorBody.messages)
        .map(([field, msg]) => `<strong>${field}:</strong> ${msg}`)
        .join('<br>');
    }

    // 2. Handle business errors or simple messages
    if (errorBody?.message && typeof errorBody.message === 'string') {
      return errorBody.message;
    }

    // 3. Handle Spring's default 'error' field
    if (errorBody?.error && typeof errorBody.error === 'string' && errorBody.error !== 'Validation Error' && errorBody.error !== 'Business Error') {
      return errorBody.error;
    }

    // 4. Handle cases where errorBody itself is a string
    if (typeof errorBody === 'string') {
      return errorBody;
    }

    // 5. Ultimate fallback to HTTP message
    return err?.message || 'An unexpected error occurred.';
  }

  addTask(): void {
    this.popupService.close();

    this.taskService.addTaskToPlanning(this.planningId, this.newTask).subscribe({
      next: () => {
        this.newTask = {
          title: '',
          description: '',
          taskDate: '',
          startTime: '',
          endTime: '',
          priority: 'MEDIUM',
          status: 'TODO'
        };

        this.loadTasks();
        this.loadProgress();
        this.loadWeightedProgress();
        this.loadDailyLoad();
        this.loadEfficiency();

        this.popupService.success('Success', 'Task added successfully.');
      },
      error: (err: any) => {
        console.error('Erreur ajout task', err);
        this.popupService.error('Validation Error', this.formatBackendError(err));
      }
    });
  }

  startEditTask(task: Task): void {
    this.popupService.close();

    this.editingTaskId = task.id ?? null;
    this.editTaskData = {
      id: task.id,
      title: task.title,
      description: task.description,
      taskDate: task.taskDate,
      startTime: task.startTime,
      endTime: task.endTime,
      priority: task.priority,
      status: task.status
    };
  }

  cancelEditTask(): void {
    this.editingTaskId = null;
    this.editTaskData = {
      id: 0,
      title: '',
      description: '',
      taskDate: '',
      startTime: '',
      endTime: '',
      priority: 'MEDIUM',
      status: 'TODO'
    };
    this.popupService.close();
  }

  updateTask(): void {
    if (!this.editingTaskId) return;

    this.popupService.close();

    this.taskService.updateTask(this.editingTaskId, this.editTaskData).subscribe({
      next: () => {
        this.cancelEditTask();
        this.loadTasks();
        this.loadProgress();
        this.loadWeightedProgress();
        this.loadDailyLoad();
        this.loadEfficiency();

        this.popupService.success('Success', 'Task updated successfully.');
      },
      error: (err: any) => {
        console.error('Erreur update task', err);
        this.popupService.error('Validation Error', this.formatBackendError(err));
      }
    });
  }

  deleteTask(id: number | undefined): void {
    if (!id) return;

    this.popupService.confirm(
      'Delete Task',
      'Are you sure you want to delete this task?',
      () => {
        this.taskService.deleteTask(id).subscribe({
          next: () => {
            this.loadTasks();
            this.loadProgress();
            this.loadWeightedProgress();
            this.loadDailyLoad();
            this.loadEfficiency();

            if (this.editingTaskId === id) {
              this.cancelEditTask();
            }

            this.popupService.success('Deleted', 'Task deleted successfully.');
          },
          error: (err: any) => {
            console.error('Erreur suppression task', err);
            this.popupService.error('Error', 'Unable to delete task.');
          }
        });
      }
    );
  }
}