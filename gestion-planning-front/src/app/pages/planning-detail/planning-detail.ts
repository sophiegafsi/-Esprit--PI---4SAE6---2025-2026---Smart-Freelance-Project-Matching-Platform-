import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Planning } from '../../models/planning.model';
import { Task } from '../../models/task.model';
import { PlanningService } from '../../services/planning';
import { TaskService } from '../../services/task';

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

  tasks: Task[] = [];

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
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    this.planningId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPlanning();
    this.loadTasks();
  }

  loadPlanning(): void {
    this.planningService.getPlanningById(this.planningId).subscribe({
      next: (data: Planning) => {
        this.planning = data;
      },
      error: (err: any) => {
        console.error('Erreur chargement planning', err);
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
      }
    });
  }

  addTask(): void {
    if (!this.newTask.title.trim()) {
      alert('Task title is required');
      return;
    }

    if (!this.newTask.description.trim()) {
      alert('Task description is required');
      return;
    }

    if (!this.newTask.taskDate || !this.newTask.startTime || !this.newTask.endTime) {
      alert('Date and time are required');
      return;
    }

    if (this.newTask.endTime <= this.newTask.startTime) {
      alert('End time must be after start time');
      return;
    }

    this.taskService.addTaskToPlanning(this.planningId, this.newTask).subscribe({
      next: () => {
        alert('Task added successfully');
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
      },
      error: (err: any) => {
        console.error('Erreur ajout task', err);
        alert(JSON.stringify(err.error));
      }
    });
  }

  startEditTask(task: Task): void {
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
  }

  updateTask(): void {
    if (!this.editingTaskId) return;

    if (!this.editTaskData.title.trim()) {
      alert('Task title is required');
      return;
    }

    if (!this.editTaskData.description.trim()) {
      alert('Task description is required');
      return;
    }

    if (!this.editTaskData.taskDate || !this.editTaskData.startTime || !this.editTaskData.endTime) {
      alert('Date and time are required');
      return;
    }

    if (this.editTaskData.endTime <= this.editTaskData.startTime) {
      alert('End time must be after start time');
      return;
    }

    this.taskService.updateTask(this.editingTaskId, this.editTaskData).subscribe({
      next: () => {
        alert('Task updated successfully');
        this.cancelEditTask();
        this.loadTasks();
      },
      error: (err: any) => {
        console.error('Erreur update task', err);
        alert(JSON.stringify(err.error));
      }
    });
  }

  deleteTask(id: number | undefined): void {
    if (!id) return;

    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(id).subscribe({
        next: () => {
          this.loadTasks();
          if (this.editingTaskId === id) {
            this.cancelEditTask();
          }
        },
        error: (err: any) => {
          console.error('Erreur suppression task', err);
        }
      });
    }
  }
}